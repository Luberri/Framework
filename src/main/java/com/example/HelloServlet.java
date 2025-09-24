package com.example;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "HelloServlet", urlPatterns = "/hello")
public class HelloServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/plain;charset=UTF-8");
        String uri = req.getRequestURI();
        String qs = req.getQueryString();
        try (PrintWriter out = resp.getWriter()) {
            out.println();
            out.println("Requested URI: " + uri);
            if (qs != null && !qs.isEmpty()) {
                out.println("Query string: "+ qs);
            }
        }
    }
}
