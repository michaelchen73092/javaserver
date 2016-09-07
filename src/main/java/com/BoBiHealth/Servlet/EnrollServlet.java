package com.BoBiHealth.Servlet;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.amazonaws.services.dynamodbv2.model.*;
import com.BoBiHealth.Doctor.*;
import com.BoBiHealth.Check.*;
import com.BoBiHealth.dynamoDB.*;
import org.json.*;
import bolts.*;
import java.util.*;
import java.math.*;
//This servlet is to allow patien submite enroll doctor's appoinmenet or online quota.
@SuppressWarnings("serial")
public  class EnrollServlet extends HttpServlet
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
        AppointCheckManager.addAppoint(doctor);
    	response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("<h1>Hello from HelloServlet</h1>");
        if(doctor != null)response.getWriter().printf("<h2>%s</h2>",doctor);
		
    
    }
    //item's format(information type)
  	//item -ItemV2 doctor
    //     -ItemV2 patient
    //     -
 
    @Override
    protected void doPost( HttpServletRequest request,
                          HttpServletResponse response ) throws ServletException,
                                                        IOException
    {
    	ItemV2 item = null;
    	try{
    		item = transForm.requestBody_toItemV2(request);
    	}catch(Exception exception){
    		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        	response.setContentType("text/html");
            response.getWriter().println("<h1>inValid Request Body</h1>");
    		return;
    	}
	    System.out.printf("method: %s\n", request.getMethod());
        System.out.printf("PathInfo: %s\n",request.getPathInfo());
    	System.out.println("get called");
    	String path_info = request.getPathInfo();
    	switch (path_info) {
			case "online":
				boolean flag = addOnlinePatient(item);
				if(flag){
			    	response.setContentType("text/html");
			        response.setStatus(HttpServletResponse.SC_OK);
			        response.getWriter().println("<h1>Hello from HelloServlet</h1>");
				}else{
			    	response.setContentType("text/html");
			        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			        response.getWriter().println("<h1>"+"No available openings"+"</h1>");
				}
		        break;
			case "appointment":
				//dummy case, the appointment is supposed to be handled by app itself.
		    	response.setContentType("text/html");
		        response.setStatus(HttpServletResponse.SC_OK);
		        response.getWriter().println("<h1>Hello from HelloServlet</h1>");
				break;
		default:
    		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        	response.setContentType("text/html");
            response.getWriter().println("<h1>inValid Request Body</h1>");
			break;
		}

    }
    //Add unappointment patient to the queue only if the unappointment patient's number has not reached 
    //the threshold
    boolean addOnlinePatient(ItemV2 item)throws ServletException,
    IOException{
    	ItemV2 doctor = (ItemV2) item.get("doctor");
    	String email = (String) doctor.get("email");
    	ItemV2 patient = (ItemV2) item.get("patient");
    	doctorAssistance[] pipe = doctorAssistance.assistanceMap.get(email);
    	return pipe[0].addOnlinePatient(patient);
    	
    }

}