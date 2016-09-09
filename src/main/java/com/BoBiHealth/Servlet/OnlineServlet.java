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
//This servlet is to allow doctor to turn their status online and modify the threshold of unappointment patient 
//in waitlist
@SuppressWarnings("serial")
public  class OnlineServlet extends HttpServlet
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
    	String pathinfo = request.getPathInfo();
    	switch (pathinfo) {
		case "addAppoint":
			addAppointment(doctor, response);
			break;
		case "waitlist":
			fetchWaitlist(doctor, response);
			break;
		default:
			break;
		}

		
    
    }
    //add this doctor to AppointCheckManager's list
    public void addAppointment(String doctor,HttpServletResponse response)throws ServletException,
    IOException
    {
        AppointCheckManager.addAppoint(doctor);
    	response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("<h1>Hello from HelloServlet</h1>");
        if(doctor != null)response.getWriter().printf("<h2>%s</h2>",doctor);
    }
    //append this doctor's waitlist to response
    public void fetchWaitlist(String doctor,HttpServletResponse response)throws ServletException,IOException{
    	doctorAssistance[] pipe = doctorAssistance.assistanceMap.get(doctor);
		ItemV2 result;
    	if(pipe != null){
    		result = pipe[0].fetchWaitlist();
        	response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println(result.String());
    	}else{
        	response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    	}
    }
    //item's format(information type)
  	//all information scatter in item. besides that, there's a item called doctor including all doctor's information
 
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
			case "turnonline":
				turnOnline(item);
		    	response.setContentType("text/html");
		        response.setStatus(HttpServletResponse.SC_OK);
		        response.getWriter().println("<h1>Hello from HelloServlet</h1>");
				break;
			case "modifyopenings":
				modifyOpening(item);
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
    void turnOnline(ItemV2 item)throws ServletException,
    IOException{
    	BigDecimal openings = (BigDecimal)item.get("openings");
    	ItemV2 doctor = (ItemV2) item.get("doctor");
    	String email = (String) doctor.get("email");
    	doctorAssistance[] pipe = doctorAssistance.assistanceMap.get(email);
     	synchronized (pipe) {
        	if(pipe[0] == null){
        		pipe[0] = new doctorAssistance((ItemV2)item.get("doctor"), pipe);
        	}
        	pipe[0].turnOnline(item);
		}
    	
    }
    void modifyOpening(ItemV2 item){
    	ItemV2 doctor = (ItemV2) item.get("doctor");
    	String email = (String) doctor.get("email");
    	doctorAssistance[] pipe = doctorAssistance.assistanceMap.get(email);
    	pipe[0].turnOnline(item);
    }
}