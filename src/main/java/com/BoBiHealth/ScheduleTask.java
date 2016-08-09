package com.BoBiHealth;
import java.util.*;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;

public class ScheduleTask extends TimerTask{
	private int num;
	private int sleep;
	private Integer[] count;
	public void run(){
		System.out.println("initialization time:"+(new Date()).toString());
		Item item = null;
		dynamoDBManager dynamoDB_inst = dynamoDBManager.instance();
		ItemCollection<ScanOutcome> results = null;
		Iterator<Item> iterator = null;
		//String token = null;
		boolean flag =false;
		ArrayList<HashSet<Item>> jobs = null;
		HashSet<Item> job = null;
		
		for(int i=0;i<3000;i++){
			synchronized (this.count) {
				System.out.printf("inner count: %d\n",this.count[0]);
				this.count[0]++;
			}
			try{
				Thread.sleep(3);
			}catch(Exception e){
				System.out.println(e.getMessage());
			}
		}
		return;
		/*
		while(!flag){
			results = dynamoDB_inst.Scan("APNs");
			iterator = results.iterator();
			jobs = new ArrayList<HashSet<Item>>();
			job = new HashSet<Item>();
			try{
				while(iterator.hasNext()){
					item = iterator.next();
					job.add(item);
					if(job.size()==num){
						jobs.add(job);
						job = new HashSet<Item>();
					}
					//logger.info(threadName+" logging!");
				}
				if(job.size()>0){
					jobs.add(job);
				}
				flag = true;
			}catch(com.amazonaws.AmazonClientException exception){
				System.out.println(exception.getMessage());
				exception.getStackTrace();
			}
		}
		int threadNum = jobs.size();
		threadManager inst = threadManager.instance();
		ArrayList<String> threadNames = new ArrayList<String>();
		for(int i=1;i<=threadNum;i++){	
			threadNames.add(inst.initNewThread(jobs.get(i-1)));
		}
		inst.startThread();
		try{
			Thread.sleep(sleep);
		}catch(InterruptedException exception){
			System.out.println(exception.getMessage());
			exception.getStackTrace();
			threadManager.instance().stopThread();
		}
		threadManager.instance().stopThread();
		//test if thread is alive and clear threadManager's List
		 try{
			threadManager.instance().isAlive();
		 }catch(Exception exception){
			 System.out.println(exception.getMessage());
			 exception.getStackTrace();
		 }
		 */
	}
	public ScheduleTask(int num,int sleep,Integer[] count){
		this.num = num;
		this.sleep = sleep;
		this.count = count;
	}
}
