package com.aaltoasia;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by romanfilippov on 22/11/15.
 */
public class ACServlet extends HttpServlet {

    public void init() throws ServletException
    {
        // Do required initialization
    }

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws ServletException, IOException
    {
//        response.setContentType("text/html");

        RequestDispatcher view = request.getRequestDispatcher("/index.html");
        view.forward(request, response);
    }

    public void destroy()
    {
        // do nothing.
    }
}
