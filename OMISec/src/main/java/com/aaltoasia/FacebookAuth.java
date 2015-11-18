package com.aaltoasia;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.FacebookApi;
import org.scribe.model.*;
import org.scribe.oauth.OAuthService;

/**
 * Created by romanfilippov on 12/11/15.
 */
public class FacebookAuth {

    private final String apiKey;
    private final String apiSecret;
    private final String apiCallback;

    private Token accessToken;

    private static final Token EMPTY_TOKEN = null;

    private final OAuthService service;

    public FacebookAuth(String apiKey, String apiSecret, String callback)
    {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.apiCallback = callback;
        this.accessToken = null;

        this.service = new ServiceBuilder()
                .provider(FacebookApi.class)
                .apiKey(apiKey)
                .apiSecret(apiSecret)
                .callback(callback)
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

    public String getUserInformation(String graphAPIPath)
    {
        OAuthRequest request = new OAuthRequest(Verb.GET, graphAPIPath);
        service.signRequest(accessToken, request);
        Response response = request.send();
        return response.getBody();
    }
}
