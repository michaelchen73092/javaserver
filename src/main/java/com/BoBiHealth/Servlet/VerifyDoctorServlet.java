package com.BoBiHealth.Servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.BoBiHealth.Doctor.*;
import com.BoBiHealth.Check.*;
import com.BoBiHealth.dynamoDB.*;
import com.amazonaws.services.dynamodbv2.model.*;
import org.json.*;
import bolts.*;
import java.util.*;
import java.math.*;
@SuppressWarnings("serial")
public  class VerifyDoctorServlet extends HttpServlet
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
    	String pathInfo = request.getPathInfo();

    	switch (pathInfo) {
		case "verify":
			verifyDoctor(doctor, response);
			break;
		case "erase":
			erase(doctor, response);
			break;
		default:
	    	response.setContentType("text/html");
	        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	        response.getWriter().println("<h1>"+"Invalid path"+"</h1>");
			break;
		}
		return;
    
    }
    private void verifyDoctor(String doctor,HttpServletResponse response)throws ServletException,
    IOException{
    	UpdateItemRequest updateItemRequest = new UpdateItemRequest();
    	Map<String, Object> key_map = new HashMap<>();
    	key_map.put("email", "doctor");
    	Map<String, Object> value_map = new HashMap<>();
    	value_map.put("doctorCertificated", new Boolean(true));
    	updateItemRequest.withKey((new ItemV2(key_map)).toAttributeValueMap());
    	updateItemRequest.setAttributeUpdates((new ItemV2(value_map)).toAttributeValueUpdate(AttributeAction.PUT));
    	updateItemRequest.withTableName("Doctors");
		Task<Object> task = dynamoDBManager.instance().updateItemAsync(updateItemRequest);
		try{
			task.waitForCompletion();
		}catch(InterruptedException exception){
	    	response.setContentType("text/html");
	        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	        response.getWriter().println("<h1>Get interrupted</h1>");
	        return;
		}
		if(task.isFaulted()){
	    	response.setContentType("text/html");
	        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	        response.getWriter().println("<h1>"+task.getError().getMessage()+"</h1>");
	        return;
		}else{
	    	synchronized (doctorAssistance.assistanceMap) {
				doctorAssistance.assistanceMap.put(doctor, new doctorAssistance[1]);
			}
	    	response.setContentType("text/html");
	        response.setStatus(HttpServletResponse.SC_OK);
	        response.getWriter().println("<h1>"+"Manage to verify"+"</h1>");
		}

    }
    private void erase(String doctor,HttpServletResponse response)throws ServletException,
    IOException{
    	DeleteItemRequest deleteItemRequest = new DeleteItemRequest();
    	deleteItemRequest.withTableName("Doctors");
    	Map<String, Object> key_map = new HashMap<>();
    	key_map.put("email", doctor);
    	Task<Object> task = dynamoDBManager.instance().deleteItemAsync(deleteItemRequest);
    	try{
    		task.waitForCompletion();
    	}catch(InterruptedException exception){
	    	response.setContentType("text/html");
	        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	        response.getWriter().println("<h1>"+"Get interrupted"+"</h1>");
    		return;
    	}
    	if(task.isFaulted()){
	    	response.setContentType("text/html");
	        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	        response.getWriter().println("<h1>"+task.getError().getMessage()+"</h1>");
	        return;
		}else{
	    	response.setContentType("text/html");
	        response.setStatus(HttpServletResponse.SC_OK);
	        response.getWriter().println("<h1>"+"Manage to erase"+"</h1>");
	        return;
		}
    }
}
