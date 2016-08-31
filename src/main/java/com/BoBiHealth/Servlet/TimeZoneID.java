package com.BoBiHealth.Servlet;
import java.util.*;
import java.math.*;
import java.io.*;
public class TimeZoneID {
	public static TimeZoneID instance = new TimeZoneID();
	public final HashSet<BigDecimal> id_map;
	public TimeZoneID(){
		id_map = new HashSet<>();
		
		FileReader fileReader;
		BufferedReader bufferedReader;

		try{
			bufferedReader= new BufferedReader(new FileReader("H:\\統計資料\\GitHub\\javaserver\\target\\classes\\timezone.txt"));
		}catch(Exception exception){
			System.out.println("cannot open the file");
			return;
		}
		String line;
		try{
	        while ((line = bufferedReader.readLine()) != null){
	        	BigDecimal temp_num = new BigDecimal(line);
	        	id_map.add(temp_num);
	        	System.out.printf("BigDecimal is %s\n",temp_num.toString());
	        }
		}catch(Exception exception){
			
		}
	}
}
