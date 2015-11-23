package com.aaltoasia;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by romanfilippov on 18/11/15.
 */
public class AuthService {

    private static final AuthService instance = new AuthService();
    private AuthService() {
        if (DBHelper.getInstance() == null)
        {
            System.out.println("Can not initiate DB. Exiting...");
            System.exit(0);
        }
    }

    public static AuthService getInstance() {
        return instance;
    }

    public OMIUser authenticateUser(String userData)
    {
        OMIUser user = new OMIUser(OMIUser.OMIUserType.OAuth);
        user.isUserAuthorized = true;

        return user;
    }

    public static void main(String[] args) {
        try {
            AuthService.getInstance().requestObjects();
        }catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

    public void writeObjectPermission(OMIObject obj, String groupName)
    {
        String objName = obj.getId();
        if (objName != null) {

            boolean writable = objName.indexOf("[W]") > -1;
            boolean readable = objName.indexOf("[R]") > -1;
            objName = objName.replace("[W]","").replace("[R]","");
            if (writable)
                DBHelper.getInstance().updateOrCreateRule(objName, groupName, writable);
            else {
                if (readable) {
                    DBHelper.getInstance().updateOrCreateRule(objName, groupName, false);
                }
            }
        }

        writePermissions(obj.getSubObjects(), groupName);
    }

    public void writePermissions(ArrayList<OMIObject> mainObj, String groupName)
    {
        for (OMIObject obj:mainObj) {
            writeObjectPermission(obj, groupName);
        }
    }


    /** for test usage **/
    public void requestObjects() throws Exception {

        URL obj = new URL(ConfigHelper.hostAddress);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add request header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", ConfigHelper.USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setRequestProperty("Content-Type", "application/xml");

        String url_body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                            "<omi:omiEnvelope xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:omi=\"omi.xsd\" version=\"1.0\" ttl=\"0\">\n" +
                            "  <omi:read msgformat=\"odf\">\n" +
                            "    <omi:msg>\n" +
                            "      <Objects xmlns=\"odf.xsd\"/>\n" +
                            "    </omi:msg>\n" +
                            "  </omi:read>\n" +
                            "</omi:omiEnvelope>";

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(url_body);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + ConfigHelper.USER_AGENT);
        System.out.println("Post parameters : " + url_body);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        System.out.println("Response BODY:");

        StringReader reader = new StringReader(response.toString());

        XMLInputFactory xif = XMLInputFactory.newInstance();
        XMLStreamReader xsr = xif.createXMLStreamReader(reader);
        for (int i = 0; i < 7; i++) {
            xsr.nextTag();
        }


        JAXBContext jc = JAXBContext.newInstance(OMIObjects.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();

        OMIObjects objResponse = (OMIObjects)unmarshaller.unmarshal(xsr);

        //System.out.println(objResponse);

    }
}
