package com.BoBiHealth;

public class versionNum {
	private Integer first,middle,end;
	public  versionNum(Integer first, Integer middle, Integer end) throws Exception{
		if(first>999 || middle>999 || end>999){
			Exception exception = new Exception(String.format("The version number is out of predefined scope\nfirst:%d\nmiddle:%d\nend:%s\n",first,middle,end));
			throw exception;
		}
		this.first = first;
		this.middle = middle;
		this.end = end;
	}
	public String ToString(){
		return String.format("%1$03d", first.intValue())+"."+String.format("%1$03d", middle.intValue())+"."+String.format("%1$03d", end.intValue());
	}
	private void evolve()throws Exception{
		if(end>999){
			middle += 1;
			end =0;
		}
		if(middle>999){
			first += 1;
			middle =0;
		}
		if(first>999){
			Exception exception = new Exception(String.format("The version number is out of predefined scope\nfirst:%d\nmiddle:%d\nend:%s\n",first,middle,end));
			throw exception;
		}
	}
	public static void main(String args[]) throws Exception {
		versionNum test = new versionNum(999, 956, 999);
		while(true){
			test.updateVersion();
			System.out.println(test.ToString());
		}
	}
	public void updateVersion()throws Exception{
		end += 1;
		evolve();
	}
	
}
