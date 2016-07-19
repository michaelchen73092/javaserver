package com.BoBiHealth;

public enum Op{
	 eq  ("="),
	 le  ("<="),
	 ls  ("<"),
	 ge  (">="),
	 gt  (">"),
	 ne  ("<>"),
	 not_exit ("if_not_exists"),
	 append  ("list_append");
	 private final String value;
	 Op(String value){
		 this.value = value;
	 }
	 public String rawValue(){
		 return this.value;
	 }
}