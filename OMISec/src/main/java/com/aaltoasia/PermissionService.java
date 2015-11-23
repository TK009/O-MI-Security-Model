package com.aaltoasia;

import com.google.gson.Gson;

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

    protected void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws ServletException, IOException
    {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String readUsers = request.getParameter("readUsers");
        if (readUsers != null) {

            PrintWriter out = response.getWriter();

            ArrayList<OMIUser> users = DBHelper.getInstance().getUsers();
            out.println(new Gson().toJson(users));

//            if (users == null)
//            {
//                out.println("{'error':'no users found'}");
//            }
        }
    }

    protected void doPost(HttpServletRequest request,
                      HttpServletResponse response)
            throws ServletException, IOException
    {
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        String groupName = request.getParameter("groupName");
        System.out.println("Received security policies for group:"+groupName);

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
            for (int i = 0; i < 4; i++) {
                xsr.nextTag();
            }


            JAXBContext jc = JAXBContext.newInstance(OMIObjects.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();

            OMIObjects objResponse = (OMIObjects) unmarshaller.unmarshal(xsr);
            String answer = "XML with permissions parsed successfully. Top objects:"+objResponse.getObjects().size();
            System.out.println(answer);
            AuthService.getInstance().writePermissions(objResponse.getObjects(), groupName);
            response.getWriter().write(answer);

        } catch (Exception ex)
        {
            System.out.println(ex.getCause()+ex.getMessage());
            response.getWriter().write("ERROR!"+ex.getCause()+ex.getMessage());
        }
    }

    public void destroy()
    {
        // do nothing.
    }
}
