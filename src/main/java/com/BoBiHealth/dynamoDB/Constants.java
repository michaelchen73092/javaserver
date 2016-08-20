package com.BoBiHealth.dynamoDB;
import java.util.*;
public class Constants {
	private static Constants inst=null;
	private HashMap<String, ArrayList<String>> keyDict = new HashMap<String, ArrayList<String>>();
	private HashMap<String,ArrayList<Type>> typeDict = new HashMap<String, ArrayList<Type>>();
	public ArrayList<String> getKeys(String tabName){
		ArrayList<String> results = keyDict.get(tabName);
		if(results==null){
			results = new ArrayList<String>();
			results.add("day");
			results.add("time");
		}
		return results;
	}
	public ArrayList<Type> getTypes(String tabName){
		ArrayList<Type> results = typeDict.get(tabName);
		if(results==null){
			results = new ArrayList<Type>();
			results.add(Type.Int);
			results.add(Type.Str);
		}
		return results;
	}
	public Constants() {
		ArrayList<String> Test2 = new ArrayList<String>();
		Test2.add("email");
		Test2.add("id");
		keyDict.put("Test2", Test2);
		ArrayList<String> APNs = new ArrayList<String>();
		APNs.add("email");
		keyDict.put("APNs", APNs);
		
		
		ArrayList<Type> Test2_type = new ArrayList<Type>();
		Test2_type.add(Type.Str);
		Test2_type.add(Type.Int);
		typeDict.put("Test2", Test2_type);
		ArrayList<Type> APNs_type = new ArrayList<Type>();
		APNs_type.add(Type.Str);
		APNs_type.add(Type.Str);
		typeDict.put("APNs", APNs_type);
		
	}
	public static Constants instance(){
		if(inst==null){
			inst = new Constants();
		}
		return inst;
		
	}
}
