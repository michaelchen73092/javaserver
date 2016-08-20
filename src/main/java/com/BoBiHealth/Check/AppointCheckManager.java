package com.BoBiHealth.Check;
import java.util.*;
import com.BoBiHealth.Doctor.*;
public class AppointCheckManager extends TimerTask{
	//public final static AppointCheckManager instance = new AppointCheckManager();
	private static HashMap<String,appointDelegate[]> appointQueue;
	private static int test=3;
	public static void addAppoint(String doctor,appointDelegate[] pipe){
		synchronized (appointQueue) {
			appointQueue.put(doctor, pipe);
		}
	}
	public static void removeAppoint(String doctor){
		synchronized (appointQueue) {
			appointQueue.remove(doctor);
		}
	}
	public void run(){
		System.out.println(AppointCheckManager.test);
	}
}
