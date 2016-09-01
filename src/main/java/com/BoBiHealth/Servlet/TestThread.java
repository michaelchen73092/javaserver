package com.BoBiHealth.Servlet;

import com.amazonaws.metrics.AwsSdkMetrics;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.local.shared.model.AttributeValue;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProvider;

import com.relayrides.pushy.apns.ApnsClient;
import com.relayrides.pushy.apns.ClientNotConnectedException;
import com.relayrides.pushy.apns.PushNotificationResponse;
import com.relayrides.pushy.apns.util.ApnsPayloadBuilder;
import com.relayrides.pushy.apns.util.SimpleApnsPushNotification;
import com.relayrides.pushy.apns.util.TokenUtil;
import io.netty.util.concurrent.Future;

import bolts.*;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutionException;
import com.BoBiHealth.dynamoDB.*;
import com.BoBiHealth.*;
import com.BoBiHealth.Check.*;

public class TestThread {
	public static Integer[] test_count = null;
	public  static class counter extends Thread{
		private static int[] lock = new int[1];
		private String threadName;
		public void run(){
			while(true){
				try{
					counting();
				}catch(InterruptedException exception){
					System.out.println("Get Interrupted!!");
					try{
						counting();
					}catch(InterruptedException exception2){
						return;
					}
					return;
				}
			}
		}
		private void counting()throws InterruptedException{
			System.out.println("Enter counting process");
			synchronized (lock) {
				System.out.printf("%s: %d\n",this.threadName,lock[0]++);
				Thread.sleep(20);
			}
		}
		public  counter(String threadName){
			this.threadName = threadName;
		}
	}
	public static void main(String args[])throws Exception{
		//AwsSdkMetrics.setCredentialProvider(new ProfileCredentialsProvider("H:\\AWSCredentials.properties","default"));
		//AwsSdkMetrics.setMetricNameSpace("TestMetric");
		//AwsSdkMetrics.setRegion(com.amazonaws.regions.Regions.US_EAST_1);
		//System.out.printf(" before defaultMetric is %b\n",AwsSdkMetrics.isMetricsEnabled());

		//System.out.printf("triiger is %b\n",AwsSdkMetrics.enableDefaultMetrics());
		//System.out.println(AwsSdkMetrics.CLOUDWATCH_REGION); 
		//System.out.printf(" defaultMetric is %b\n",AwsSdkMetrics.isMetricsEnabled());
		/*Item item = null;
		dynamoDBManager dynamoDB_inst = new dynamoDBManager();
		ItemCollection<QueryOutcome> results = dynamoDB_inst.Query("APNs","Chien-Lin","zero064@gmail.com", Op.eq);
		Iterator<Item> iterator = results.iterator();
		String token = null;
		HashSet<Item> job = new HashSet<Item>();
		while(iterator.hasNext()){
			item = iterator.next();
			job.add(item);
			token = (String) item.get("token");
			System.out.println("token: "+token);
			//logger.info(threadName+" logging!");
		}
		assert(token != null);
		System.out.println("sample token: "+TokenUtil.sanitizeTokenString("<efc7492 bdbd8209>"));
		testThread(job);
		System.out.println("the end");*/
		TimeZoneID timeZoneID = TimeZoneID.instance;
		AttributeValue attr1 = new AttributeValue();
		/*Collection<AttributeValue> coll = new ArrayList<AttributeValue>();
		coll.add((new AttributeValue()).withS("test1"));
		coll.add((new AttributeValue()).withS("test2"));
		attr1.setL(coll);
		System.out.printf("AttritubeValue's String:%s\n",attr1.toString());
		HashSet<Integer> set = new HashSet<>();
		System.out.printf("current directory:%s\n", System.getProperty("user.dir"));
		AttributeValue attr2 = new AttributeValue(attr1.toString());
		System.out.printf("the passed:%s\n",attr2.toString());
		List<AttributeValue> test_list = attr2.getL();*/
		//System.out.printf("first:%s",test_list.get(0).getS());
		//System.out.printf("second:%s",test_list.get(1).getS());
		counter th1 = new counter("thread1");
		counter th2 = new counter("thread2");
		th1.start();
		Thread.sleep(5000);
		th1.interrupt();
		System.out.printf("wall time%d\n", System.currentTimeMillis());
		Task<Object> task = dynamoDBManager.instance().Query("APNs", "zero064@gmail.com", null, Op.eq).continueWithTask(new Continuation<CollectionWrapper<ItemV2>, Task<Object>>(){
			public Task<Object> then(Task<CollectionWrapper<ItemV2>> task)throws Exception{
				if(!task.isFaulted()){
					Exception exception  = new Exception("test exception");
					System.out.println("throw exception");
					System.out.println(task.getError().getMessage());
					throw exception;
				}

				return null;
			}
		});
		if(task == null) System.out.println("task is null");
		task.waitForCompletion();
		System.out.println("task complete");
		Thread.sleep(600000);
		test_count = new Integer[1];
		test_count[0] = 0;
		Timer timer = new Timer();
		timer.schedule(new ScheduleTask(1,300000,test_count), 2, 600000);
		/*System.out.println("Imediately");
		Thread.sleep(100);
		for(int i=0;i<2000;i++){
			synchronized (test_count[0]) {
				System.out.printf("In upper side count: %d\n", test_count[0]);
				test_count[0]++;
			}
			Thread.sleep(3);

		}*/
		/*
		Thread.sleep(20000);
		System.out.println("test alive");
		threadManager.instance().stopThread();
		Thread.sleep(1000);
		threadManager.instance().isAlive();
		*/

	}
	public static void testThread(HashSet<ItemV2> job)throws Exception{
		threadManager inst = threadManager.instance();
		ArrayList<String> threadNames = new ArrayList<String>();
		threadNames.add(inst.initNewThread(job));
		threadNames.add(inst.initNewThread(job));
		inst.startThread();
		//threadManager.instance().isAlive();;

	
		return;
	}
}
