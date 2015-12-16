package com.aaltoasia;

import com.google.gson.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;

/**
 * Created by romanfilippov on 23/11/15.
 */
public class PermissionService extends HttpServlet {

    public void init() throws ServletException
    {
        // Do required initialization
    }


    // Wrap array with top-level key
    private String wrapJson(Object objectToSerialize, String keyName) {
        Gson gson = new Gson();
        JsonObject result = new JsonObject();
        //Obtain a serialized version of your object
        JsonElement jsonElement = gson.toJsonTree(objectToSerialize);
        result.add(keyName, jsonElement);
        return gson.toJson(result);
    }

    protected void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws ServletException, IOException
    {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String readUsers = request.getParameter("readUsers");
        String readGroups = request.getParameter("readGroups");
        String removeGroups = request.getParameter("removeGroups");
        String readRules = request.getParameter("readRules");
        if (readUsers != null) {

            PrintWriter out = response.getWriter();

            ArrayList<OMIUser> users = DBHelper.getInstance().getUsers();
            out.println(wrapJson(users, "users"));

        } else if (readGroups != null) {

            PrintWriter out = response.getWriter();

            ArrayList<OMIGroup> groups = DBHelper.getInstance().getGroups();
            out.println(wrapJson(groups, "groups"));
        } else if (readRules != null) {

            int groupID = Integer.parseInt(request.getParameter("groupID"));
            PrintWriter out = response.getWriter();
            ArrayList<OMIRule> rules = DBHelper.getInstance().getRules(groupID);

            out.println(wrapJson(rules, "rules"));
        } else if (removeGroups != null) {

            int groupID = Integer.parseInt(request.getParameter("groupID"));
            PrintWriter out = response.getWriter();
            if (DBHelper.getInstance().deleteGroup(groupID))
                out.write("{\"result\":\"ok\"}");
            else
                out.write("{\"error\":\"group was not deleted\"}");

        }
    }

    protected void doPost(HttpServletRequest request,
                      HttpServletResponse response)
            throws ServletException, IOException
    {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String writeRules = request.getParameter("writeRules");
        String writeGroups = request.getParameter("writeGroups");

        if (writeRules != null) {

            String groupID = request.getParameter("groupID");
            System.out.println("Received security policies for group with ID:" + groupID);

            StringBuffer jb = new StringBuffer();
            String line = null;
            try {
                BufferedReader reader = request.getReader();
                while ((line = reader.readLine()) != null)
                    jb.append(line);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            StringReader reader = new StringReader(jb.toString());

            try {

                XMLInputFactory xif = XMLInputFactory.newInstance();
                XMLStreamReader xsr = xif.createXMLStreamReader(reader);

                while (xsr.hasNext()) {
                    if (xsr.isStartElement() && "Objects".equals(xsr.getLocalName())) {
                        break;
                    }
                    xsr.next();
                }


                JAXBContext jc = JAXBContext.newInstance(OMIObjects.class);
                Unmarshaller unmarshaller = jc.createUnmarshaller();

                OMIObjects objResponse = (OMIObjects) unmarshaller.unmarshal(xsr);
                String answer = "XML with permissions parsed successfully. Objects:" + objResponse.getObjects().size();
                System.out.println(answer);

                writeXPath(objResponse);

                AuthService.getInstance().writePermissions(objResponse.getObjects(), Integer.parseInt(groupID));
                response.getWriter().write(answer);

            } catch (Exception ex) {
                System.out.println(ex.getCause() + ex.getMessage());
                response.getWriter().write("ERROR!" + ex.getCause() + ex.getMessage());
            }
        } else if (writeGroups != null) {

            StringBuffer jb = new StringBuffer();
            String line = null;
            try {
                BufferedReader reader = request.getReader();
                while ((line = reader.readLine()) != null)
                    jb.append(line);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }


            JsonObject newGroup = new JsonParser().parse(jb.toString()).getAsJsonObject();
            String groupName = newGroup.getAsJsonPrimitive("name").getAsString();

            JsonArray userIDs_json = newGroup.getAsJsonArray("values");
            int [] userIDs = new int[userIDs_json.size()];
            for (int i = 0; i < userIDs_json.size(); i++) {
                userIDs[i] = userIDs_json.get(i).getAsInt();
            }

            int newGroupID = -1;
            if (newGroup.getAsJsonPrimitive("id") != null)
            {
                newGroupID = newGroup.getAsJsonPrimitive("id").getAsInt();
            }

            if (newGroupID == -1) {
                System.out.println("Creating new group for name:"+groupName);
                newGroupID = DBHelper.getInstance().createGroup(groupName);

                if (newGroupID == -1) {
                    response.getWriter().write("{\"error\":\"new group was not created\"}");
                } else {

                    if (userIDs.length > 0) {
                        if (!DBHelper.getInstance().addUsersToGroup(userIDs,newGroupID))
                            response.getWriter().write("{\"error\":\"user list can not be added to group with id="+newGroupID+"\"}");
                    }
                    response.getWriter().write("{\"result\":\"ok\",\"groupID\":\"" + newGroupID + "\"}");
                }
            } else {
                System.out.println("Modifying group with ID:"+newGroupID);
                if (DBHelper.getInstance().updateGroup(newGroupID, groupName)) {
                    DBHelper.getInstance().updateUsersForGroup(userIDs, newGroupID);
                    response.getWriter().write("{\"result\":\"ok\"}");
                }
                else
                    response.getWriter().write("{\"error\":\"group was not updated\"}");
            }

        }
    }

    public void writeObjectXPath(OMIObject obj, String currentPath) {

        String newPath = currentPath + "/" + obj.getId().replace("[W]","").replace("[R]","");;
        obj.xPath = newPath;

        for (OMIInfoItem infoItem:obj.getInfoItems()) {
            infoItem.xPath = newPath + "/" + infoItem.getName().replace("[W]","").replace("[R]","");;
        }

        for (OMIObject nextObject:obj.getSubObjects()) {
            writeObjectXPath(nextObject,newPath);
        }
    }

    public void writeXPath(OMIObjects objects) {

        String path = "Objects";
        objects.xPath = path;

        for (OMIObject obj:objects.getObjects()) {
            writeObjectXPath(obj,path);
        }

    }

    public void destroy()
    {
        // do nothing.
    }
}
