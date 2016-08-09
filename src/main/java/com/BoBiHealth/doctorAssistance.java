package com.BoBiHealth;

import java.util.*;

public class doctorAssistance extends Thread {
	public static HashMap<String, doctorAssistance> assistanceMap = new HashMap<String, doctorAssistance>();
	private ArrayList<String> request_queue; 
	public static Integer testInt = new Integer(3);
	public void run(){
		while(true){
			String curr_request;
			synchronized (testInt) {
				try{
					curr_request = request_queue.remove(request_queue.size()-1);
				}catch(IndexOutOfBoundsException e){
					System.out.println("List is empty");
					try{
						Thread.sleep(20);
					}catch(InterruptedException ex){
						System.out.println("service is interupted");
						return;
					}
					continue;
				}
			}
		}
	}
	public static void main(String args[])throws Exception{
		doctorAssistance assistance = new doctorAssistance();
		
		assistance.start();
		
		Thread.sleep(40000);
		assistance.interrupt();
		synchronized (assistance.testInt) {
			System.out.println("make it!!");
		}
		
		
	}
	
	public doctorAssistance(){
		request_queue = new ArrayList<String>();
	}
}
class onWorking extends Thread{
	
}