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
@SuppressWarnings("serial")
public  class addAppointSevlet extends HttpServlet
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
			case "openAppoint":     	
				openAppoint(item, response);
		    	addAppoint_table(item);
		    	response.setContentType("text/html");
		        response.setStatus(HttpServletResponse.SC_OK);
		        response.getWriter().println("<h1>Hello from HelloServlet</h1>");
				break;
			case "removeAppoint":
				modifyAppoint_table(item,AttributeAction.DELETE);
		    	response.setContentType("text/html");
		        response.setStatus(HttpServletResponse.SC_OK);
		        response.getWriter().println("<h1>Hello from HelloServlet</h1>");
				break;
			case "addAppoint":
				modifyAppoint_table(item,AttributeAction.ADD);
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
    private void openAppoint( ItemV2 item,
            HttpServletResponse response)throws ServletException,IOException{
    	dynamoDBManager dyanamo = dynamoDBManager.instance();
    	doctorAssistance[] pipe;
    	String tabName ;
    	Task<Object> task;
    	try{
    		ItemV2 doctor = (ItemV2) item.get("doctor");
    		tabName = checkingManager.appointTabName((String) doctor.get("email"));
    		 pipe = doctorAssistance.assistanceMap.get((String)doctor.get("email"));
    		task = dyanamo.openAppoint(tabName, item);
            task.waitForCompletion();

    		
    	}catch(Exception exception){
    		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        	response.setContentType("text/html");
            response.getWriter().println("<h1>inValid Request Body</h1>");
    		return;
    	}
    	if(task.isFaulted()){
    		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        	response.setContentType("text/html");
            response.getWriter().println("<h1>"+task.getError().getMessage()+"</h1>");
    		return;
    	}
    	synchronized (pipe) {
        	if(pipe[0] == null){
        		pipe[0] = new doctorAssistance((ItemV2)item.get("doctor"), pipe);
        	}
    		pipe[0].addAppointTask();
		}
    }
    private void addAppoint_table(ItemV2 item){
    	ItemV2 value = (ItemV2) item.get("value");
    	ItemV2 doctor = (ItemV2) item.get("doctor");
    	TimeZoneID timeZoneID = TimeZoneID.instance;
    	Map<String, Object> key_map = new HashMap<>();
    	for(BigDecimal offset:timeZoneID.id_map){
        	Calendar date = setDate(item);
        	date.add(Calendar.MINUTE, offset.intValue());
        	String hashkey = offset.toString();
        	BigDecimal sortkey = new BigDecimal(date.get(Calendar.DAY_OF_MONTH));
        	BigDecimal month = new BigDecimal(date.get(Calendar.MONTH));
        	BigDecimal year = new BigDecimal(date.get(Calendar.YEAR));
        	String tabName = "Appointment";
        	String time_info = String.format("%02d", date.get(Calendar.HOUR_OF_DAY))+":"+String.format("%02d", date.get(Calendar.MINUTE));
        	key_map.put("timezone_ID", hashkey);
        	key_map.put("day",sortkey);
        	Map<String, Object> value_map = new HashMap<>();
        	Map<String, Collection<String>> time_map = new HashMap<>();
        	HashSet<String> time_set = new HashSet<>();
        	time_set.add(time_info);
        	time_map.put((String)doctor.get("email"), time_set);
        	value_map.put("time", time_map);
        	Task<CollectionWrapper<ItemV2>> task = dynamoDBManager.instance().Query(tabName, hashkey, sortkey, Op.eq);
        	Task<Object> return_task = task.continueWithTask(new Continuation<CollectionWrapper<ItemV2>, Task<Object>>(){
        		public Task<Object> then(Task<CollectionWrapper<ItemV2>> task1) throws Exception{
        			if(task1.isFaulted()){
        				Exception exception = task.getError();
        				System.out.println(exception.getMessage());
        				return null;
        			}else{
        				Iterator<ItemV2> ite = task.getResult().iterator();
        				while(ite.hasNext()){
        					ItemV2 item = ite.next();
        					return updateAppoint(item, year, month, tabName, key_map, value_map);
        				}
        			}
        			return null;
        		}
        	});
    	}
    	
    }
    private void modifyAppoint_table(ItemV2 item,AttributeAction action){
    	ItemV2 value = (ItemV2) item.get("value");
    	ItemV2 doctor = (ItemV2) item.get("doctor");
    	TimeZoneID timeZoneID = TimeZoneID.instance;
    	Map<String, Object> key_map = new HashMap<>();
    	for(BigDecimal offset:timeZoneID.id_map){
        	Calendar date = setDate(item);
        	date.add(Calendar.MINUTE, offset.intValue());
        	String hashkey = offset.toString();
        	BigDecimal sortkey = new BigDecimal(date.get(Calendar.DAY_OF_MONTH));
        	BigDecimal month = new BigDecimal(date.get(Calendar.MONTH));
        	BigDecimal year = new BigDecimal(date.get(Calendar.YEAR));
        	String tabName = "Appointment";
        	String time_info = String.format("%02d", date.get(Calendar.HOUR_OF_DAY))+":"+String.format("%02d", date.get(Calendar.MINUTE));
        	key_map.put("timezone_id", hashkey);
        	key_map.put("day",sortkey);
        	Map<String, Object> value_map = new HashMap<>();
        	Map<String, Collection<String>> time_map = new HashMap<>();
        	HashSet<String> time_set = new HashSet<>();
        	time_set.add(time_info);
        	time_map.put((String)doctor.get("email"), time_set);
        	value_map.put("time", time_map);
			UpdateItemRequest request = new UpdateItemRequest();
			request.withKey((new ItemV2(key_map)).toAttributeValueMap());
			request.withTableName(tabName);
			Map<String, AttributeValueUpdate> attrs = (new ItemV2(value_map)).toAttributeValueUpdate(action);
			request.setAttributeUpdates(attrs);
			dynamoDBManager.instance().updateItemAsync(request);
    	}
    	return;
    }
    private Task<Object> updateAppoint(ItemV2 item,BigDecimal year,BigDecimal month,String tabName,Map<String,Object>key_map,Map<String, Object>value_map){
			BigDecimal returned_month = (BigDecimal)item.get("month");
			BigDecimal returned_year = (BigDecimal) item.get("year");
			UpdateItemRequest request = new UpdateItemRequest();
			request.withKey((new ItemV2(key_map)).toAttributeValueMap());
			request.withTableName(tabName);
			if(returned_month.compareTo(month)==0 && returned_year.compareTo(year)==0){
				Map<String, AttributeValueUpdate> attrs = (new ItemV2(value_map)).toAttributeValueUpdate(AttributeAction.ADD);
				request.setAttributeUpdates(attrs);
			}else{
				value_map.put("month", month);
				value_map.put("year", year);
				Map<String, AttributeValueUpdate> attrs = (new ItemV2(value_map)).toAttributeValueUpdate(AttributeAction.PUT);
				request.setAttributeUpdates(attrs);
			}
			return dynamoDBManager.instance().updateItemAsync(request);
    }
    private Calendar setDate(ItemV2 item){
    	ItemV2 value = (ItemV2) item.get("value");
    	ItemV2 key = (ItemV2) item.get("keys");
    	BigDecimal year =(BigDecimal) value.get("year");
    	BigDecimal month =(BigDecimal) value.get("month");
    	BigDecimal day =(BigDecimal) key.get("day");
    	String time = (String) key.get("time");
    	String[] str_arry = time.split(":");
    	BigDecimal hour = new BigDecimal(str_arry[0]);
    	BigDecimal min = new BigDecimal(str_arry[1]);
    	Calendar date = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    	date.set(Calendar.YEAR, year.intValue());
    	date.set(year.intValue(), month.intValue(),day.intValue() , hour.intValue(), min.intValue(), 0);
    	return date;
    	
    }
}