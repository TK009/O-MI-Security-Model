package com.aaltoasia.omi.accontrol;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by romanfilippov on 12/11/15.
 */
public class TestAuth extends HttpServlet {


    private FacebookAuth auth;

    public void init() throws ServletException
    {
        auth = FacebookAuth.getInstance();
    }

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws ServletException, IOException
    {
        // Set response content type
        response.setContentType("text/html");

        PrintWriter out = response.getWriter();
        String title = "O-MI Login";
        String docType =
                "<!doctype html public \"-//w3c//dtd html 4.0 " +
                        "transitional//en\">\n";

        String auth_type = request.getParameter("auth_type");
        String accessCode = request.getParameter("code");
        if (accessCode != null)
        {
            auth.getAccessToken(accessCode);
            String userInfo = auth.getUserInformation();
            boolean authenticated = auth.registerOrLoginUser(userInfo);
            System.out.println(userInfo);
            out.println(docType +
                    "<html>\n" +
                    "<head><title>" + title + "</title></head>\n" +
                    "<body>\n" +
                    "<h1 align=\"center\">" + title + "</h1>\n" +
                    "<div align=\"center\">" +
                     userInfo +
                    "</div>" +
                    (authenticated ? "<h1 align=\"center\">User authenticated<br/><a href=\"http://localhost:8088/AC\">Go to AC Console</a></h1>" : "") +
                    "</body></html>");
        } else if (auth_type == null) {

            out.println(docType +
                            "<html>\n" +
                            "<head><title>" + title + "</title></head>\n" +
                            "<body>\n" +
                            "<h1 align=\"center\">" + title + "</h1>\n" +
                            "<div align=\"center\">" +
                            "<form action=\"O-MI\">" +
                            "<input type=\"hidden\" name=\"auth_type\" value=\"facebook\"/>" +
                            "<input type=\"submit\" value=\"Facebook\"/>" +
                            "</form>" +
                            "<form action=\"O-MI\">" +
                            "<input type=\"hidden\" name=\"auth_type\" value=\"shibboleth\"/>" +
                            "<input type=\"button\" value=\"Shibboleth\" onclick=\"alert('Shibboleth is not supported yet!');\"/>" +
                            "</form></div>" +
                            "</body></html>");
        } else {
            if (auth_type.equalsIgnoreCase("facebook")) {
                String authURL = auth.getAuthorizationURL();
                response.sendRedirect(authURL);
            }
            // TODO: place shibboleth auth handler here

        }
    }

    public void destroy()
    {
        // do nothing.
    }
}
