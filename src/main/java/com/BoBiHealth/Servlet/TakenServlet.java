package com.BoBiHealth.Servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.BoBiHealth.Doctor.*;
import com.BoBiHealth.Check.*;
import com.BoBiHealth.dynamoDB.*;
import org.json.*;
import bolts.*;
import java.util.*;
import java.math.*;
@SuppressWarnings("serial")
public  class TakenServlet extends HttpServlet
{
    @Override
    protected void doGet( HttpServletRequest request,
                          HttpServletResponse response ) throws ServletException,
                                                        IOException
    {
    	String doctor = request.getParameter("doctor");
 
	    System.out.printf("method: %s\n", request.getMethod());
        System.out.printf("PathInfo: %s\n",request.getPathInfo());
    	System.out.println("get called");
		doctorAssistance[] pipe = doctorAssistance.assistanceMap.get(doctor);
		synchronized (pipe) {
			if(pipe[0]==null){
		    	response.setContentType("text/html");
		        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		        response.getWriter().println("<h1>Doctor not exist</h1>");
		        return;
			}
			pipe[0].take();
		}
    	response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("<h1>Hello from HelloServlet</h1>");
		return;
    
    }
    
}
    