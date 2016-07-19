package com.BoBiHealth;

public enum LogicOp{
    AND ("AND"),
    OR ("OR");
	 private final String value;
	 LogicOp(String value){
		 this.value = value;
	 }
	 public String rawValue(){
		 return this.value;
	 }
}