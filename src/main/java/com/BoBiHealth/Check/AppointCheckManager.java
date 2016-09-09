package com.BoBiHealth.Check;
import java.util.*;
import com.BoBiHealth.Doctor.*;
public class AppointCheckManager extends TimerTask{
	//public final static AppointCheckManager instance = new AppointCheckManager();
	private static volatile HashMap<String,appointDelegate[]> appointQueue;
	private static int test=3;
	public static void addAppoint(String doctor){
		synchronized (appointQueue) {
			appointDelegate[] pipe = doctorAssistance.assistanceMap.get(doctor);
			appointQueue.put(doctor, pipe);
		}
	}
	public static void removeAppoint(String doctor){
		synchronized (appointQueue) {
			appointQueue.remove(doctor);
		}
	}
	public static void start(){
		Timer timer = new Timer();
		timer.schedule(new AppointCheckManager(),2,900000);
	}
	public void run(){
		checkingManager job = new checkingManager(appointQueue.values());
		job.start();
	}
}
