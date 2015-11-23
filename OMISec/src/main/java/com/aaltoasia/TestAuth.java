package com.aaltoasia;

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
        // Do required initialization
    }

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws ServletException, IOException
    {
        //auth = new FacebookAuth("485832608262799", "44aa147103d0a84e8b092f4465e3e58a", "http://otaniemi3d.cs.hut.fi/oauth_callback");

        // Set response content type
        response.setContentType("text/html");

        PrintWriter out = response.getWriter();
        String title = "O-MI Login";
        String docType =
                "<!doctype html public \"-//w3c//dtd html 4.0 " +
                        "transitional//en\">\n";

        String client_id = request.getParameter("client_id");
        String redirect_uri = request.getParameter("redirect_uri");
        String api_secret = request.getParameter("api_secret");
        String accessCode = request.getParameter("code");
        if (accessCode != null)
        {
            auth.getAccessToken(accessCode);
            out.println(docType +
                    "<html>\n" +
                    "<head><title>" + title + "</title></head>\n" +
                    "<body>\n" +
                    "<h1 align=\"center\">" + title + "</h1>\n" +
                    "<div align=\"center\">" +
                    auth.getUserInformation("https://graph.facebook.com/me") +
                    "</div>" +
                    "</body></html>");
        } else if (client_id == null) {

            out.println(docType +
                            "<html>\n" +
                            "<head><title>" + title + "</title></head>\n" +
                            "<body>\n" +
                            "<h1 align=\"center\">" + title + "</h1>\n" +
                            "<div align=\"center\">" +
                            "<form action=\"O-MI\">" +
                            "<input type=\"text\" placeholder=\"client id\" name=\"client_id\"/><br/>\n" +
                            "<input type=\"text\" placeholder=\"api secret\" name=\"api_secret\"/><br/>" +
                            "<input type=\"text\" placeholder=\"redirect uri\" name=\"redirect_uri\"/><br/>" +
                    "<input type=\"submit\" value=\"Go\"/>" +
                            "</form></div>" +
                            "</body></html>");
        } else if ((api_secret != null) && (redirect_uri != null)) {

            auth = new FacebookAuth(client_id,api_secret,redirect_uri);
            response.sendRedirect(auth.getAuthorizationURL());
        }
    }

    public void destroy()
    {
        // do nothing.
    }
}
