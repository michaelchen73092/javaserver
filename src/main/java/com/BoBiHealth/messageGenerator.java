package com.BoBiHealth;
import java.util.*;
import org.json.*;
public class messageGenerator {
	//true: doctor, false: user
	private boolean isDoctor;
	private JSONObject target;
	public String informDoctor(){
		String result = "#1:";
		result += target.getString("firstname")+target.getString("lastname")+",you got a patient";
		return result;
	}
	public String informPatienLeave(String firstname,String lastname){
		String result = "#2:";
		result += firstname +" "+lastname+",the doctor can not be connected.\nPlease stop waiting";
		return result;
	}
	public String informDoctorOutConnectionOffline(){
		String result = "#3:";
		result += "You're out of connection too long, your status turns offline.\nAll your patiens in wait list left\n";
		return result;
	}
	public String informDoctorOutConnection(){
		String result = "#3:";
		result += "You're out of connection too long, all your patiens in wait list left\n";
		return result;
	}
	public messageGenerator(boolean isDoctor,JSONObject target){
		this.target = target;
		this.isDoctor = isDoctor;
		
	}
}
