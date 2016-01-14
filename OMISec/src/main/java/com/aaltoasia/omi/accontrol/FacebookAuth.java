package com.aaltoasia.omi.accontrol;

import com.aaltoasia.omi.accontrol.db.DBHelper;
import com.aaltoasia.omi.accontrol.db.objects.OMIUser;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.FacebookApi;
import org.scribe.model.*;
import org.scribe.oauth.OAuthService;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper class that handles Facebook Authentication
 * Created by romanfilippov on 12/11/15.
 */
public class FacebookAuth {

    // TODO: Move apiSecret out of here
    private final String apiKey = "485832608262799";
    private final String apiSecret = "44aa147103d0a84e8b092f4465e3e58a";
    private final String apiCallback = "http://localhost:8088/O-MI";

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    // see https://developers.facebook.com/docs/facebook-login/permissions
    private final String fbScope = "email,public_profile";

    private Token accessToken;
    private static final Token EMPTY_TOKEN = null;
    private final OAuthService service;

    private static final FacebookAuth instance = new FacebookAuth();
    public static FacebookAuth getInstance() {
        return instance;
    }

    private FacebookAuth()
    {
        logger.setLevel(Level.INFO);

        this.accessToken = null;

        this.service = new ServiceBuilder()
                .provider(FacebookApi.class)
                .apiKey(apiKey)
                .apiSecret(apiSecret)
                .scope(fbScope)
                .callback(apiCallback)
                .build();
    }

    public String getAuthorizationURL()
    {
        return service.getAuthorizationUrl(EMPTY_TOKEN);
    }

    public Token getAccessToken(String authCode)
    {
        if (accessToken == null) {
            Verifier verifier = new Verifier(authCode);
            accessToken = service.getAccessToken(EMPTY_TOKEN, verifier);
        }
        return accessToken;
    }

    public String getUserInformation()
    {
        String graphAPIPath = "https://graph.facebook.com/me?fields=id,name,email";
        OAuthRequest request = new OAuthRequest(Verb.GET, graphAPIPath);
        service.signRequest(accessToken, request);
        Response response = request.send();
        return response.getBody();
    }

    public OMIUser createUserForInfo(String userData) {
        try {

            JsonObject newUserJSON = new JsonParser().parse(userData).getAsJsonObject();
            OMIUser newUser = new OMIUser(OMIUser.OMIUserType.OAuth);
            String userName = newUserJSON.getAsJsonPrimitive("name").getAsString();
            String userEmail = newUserJSON.getAsJsonPrimitive("email").getAsString();
            newUser.username = userName;
            newUser.email = userEmail;
            return newUser;
        } catch (Exception ex) {
            logger.severe(ex.getCause() + ":" + ex.getMessage());
            return null;
        }
    }

    public boolean registerOrLoginUser(OMIUser newUser) {
        try {

            return DBHelper.getInstance().createUserIfNotExists(newUser);

        } catch (Exception ex) {
            logger.severe(ex.getCause() + ":" + ex.getMessage());
            return false;
        }
    }
    public boolean registerOrLoginUser(String userData) {
        try {

            OMIUser newUser = createUserForInfo(userData);
            return DBHelper.getInstance().createUserIfNotExists(newUser);

        } catch (Exception ex) {
            logger.severe(ex.getCause() + ":" + ex.getMessage());
            return false;
        }
    }
}
