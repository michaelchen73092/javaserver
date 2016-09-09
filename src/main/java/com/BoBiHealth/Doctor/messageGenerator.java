package com.BoBiHealth.Doctor;
import java.util.*;
import org.json.*;
public class messageGenerator {
	//#1:inform doctor he got a patient
	//#2:inform patient that doctor is missing
	//#3:inform doctor, his waitlist is cleared
	//#4:improm patient their number in the waitlist
	//#5:inform doctor to refresh the waitlist
	//true: doctor, false: user
	private boolean isDoctor;
	private JSONObject target;
	public String informDoctor(){
		String result = "#1:";
		result += target.getString("firstname")+target.getString("lastname")+",you got a patient";
		return result;
	}
	public String informPatienLeave(boolean endbyDoctor,String firstname,String lastname){
		String result = "#2:";
		if(endbyDoctor){
			result += firstname +" "+lastname+",the doctor just left.\nPlease stop waiting";
		}else{
			result += firstname +" "+lastname+",the doctor can not be connected.\nPlease stop waiting";
		}
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
	public String endInterview(){
		String result = "#3:";
		result += "You've turned offline, all your patiens in wait list left\n";
		return result;
	}
	public messageGenerator(boolean isDoctor,JSONObject target){
		this.target = target;
		this.isDoctor = isDoctor;
		
	}
}
