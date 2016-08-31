package com.BoBiHealth.Servlet;
import java.io.BufferedReader;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.BoBiHealth.Doctor.*;
import com.BoBiHealth.Check.*;
import org.json.*;
import com.BoBiHealth.dynamoDB.*;
public class transForm {
	public static ItemV2 requestBody_toItemV2(HttpServletRequest baseRequest)throws Exception{
		StringBuilder jasonBuff = new StringBuilder();
        String line = null;
        try {
          BufferedReader reader = baseRequest.getReader();
          while ((line = reader.readLine()) != null)
            jasonBuff.append(line);
        } catch (Exception e) { 
        	System.out.println(e.getMessage());
        	e.getStackTrace();
        }
        String tempStr = jasonBuff.toString();
        System.out.println(tempStr);
        ItemV2 item = null;
        
    	item = new ItemV2(tempStr);

   
        return item;
	}
}
