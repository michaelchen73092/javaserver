package com.BoBiHealth.Servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.BoBiHealth.Doctor.*;
@SuppressWarnings("serial")
public  class StopServlet extends HttpServlet
{
    @Override
    protected void doGet( HttpServletRequest request,
                          HttpServletResponse response ) throws ServletException,
                                                        IOException
    {
    	String doctor = request.getParameter("doctor");
    	doctorAssistance[] assistance = doctorAssistance.assistanceMap.get(doctor);
    	synchronized (assistance) {
    	    System.out.printf("method: %s\n", request.getMethod());
            System.out.printf("PathInfo: %s\n",request.getPathInfo());
        	System.out.println("get called");
            if(!assistance[0].hasTask()){
            	assistance[0] = null;
            }
        	response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("<h1>Hello from HelloServlet</h1>");
            if(doctor != null)response.getWriter().printf("<h2>%s</h2>",doctor);
		}
    
    }
}