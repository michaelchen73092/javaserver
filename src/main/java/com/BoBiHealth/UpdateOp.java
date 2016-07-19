package com.BoBiHealth;

public enum UpdateOp{
    SET ("SET"),
    REMOVE ("REMOVE"),
    ADD ("ADD"),
    DELETE ("DELETE");
	 private final String value;
	 UpdateOp(String value){
		 this.value = value;
	 }
	 public String rawValue(){
		 return this.value;
	 }
}