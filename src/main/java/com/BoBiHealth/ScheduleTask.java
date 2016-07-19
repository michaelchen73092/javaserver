package com.BoBiHealth;
import java.util.*;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;

public class ScheduleTask extends TimerTask{
	private int num;
	private ArrayList<HashSet<Item>> jobs;
	public void run(){
		threadManager inst = threadManager.instance();
		ArrayList<String> threadNames = new ArrayList<String>();
		for(int i=1;i<=num;i++){	
			threadNames.add(inst.initNewThread(jobs.get(i-1)));
		}
		inst.startThread();
		try{
			Thread.sleep(600000);
		}catch(InterruptedException exception){
			System.out.println(exception.getMessage());
			exception.getStackTrace();
			threadManager.instance().stopThread();
		}
		threadManager.instance().stopThread();

	}
	public ScheduleTask(int num, ArrayList<HashSet<Item>> jobs){
		this.num = num;
		this.jobs = jobs;
	}
}
