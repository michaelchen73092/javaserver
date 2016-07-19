package com.BoBiHealth;

import java.text.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.Calendar;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.math.BigDecimal;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.metrics.AwsSdkMetrics;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.*;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.LocalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProjectionType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.relayrides.pushy.apns.ApnsClient;
import com.relayrides.pushy.apns.ClientNotConnectedException;
import com.relayrides.pushy.apns.PushNotificationResponse;
import com.relayrides.pushy.apns.util.ApnsPayloadBuilder;
import com.relayrides.pushy.apns.util.SimpleApnsPushNotification;

import io.netty.util.concurrent.Future;

public class dynamoDBthread extends Thread {
	private String threadName;
	private HashSet<Item> job;
	private Calendar date;
    private static final Logger logger = LogManager.getLogger(dynamoDBthread.class);
	dynamoDBthread(String name,HashSet<Item> job,Calendar date){
		this.threadName = name;
		this.job = job;
		this.date = date;
		assert(job !=null);
		System.out.println("Creating "+threadName);
	}
	public void run(){
		//System.out.printf(threadName+" defaultMetric is %b\n",AwsSdkMetrics.isMetricsEnabled());
		//System.out.println("Running "+threadName);
		dynamoDBManager dynamoDB_inst = new dynamoDBManager();
		ApnsClient<SimpleApnsPushNotification> apnsClient;
		try{
		apnsClient = new ApnsClient<SimpleApnsPushNotification>(
		        new File("H:\\Certificate\\Certificate\\Certificates.p12"),"Cc5302196029");
		final Future<Void> connectFuture = apnsClient.connect(ApnsClient.DEVELOPMENT_APNS_HOST);
		connectFuture.await();
		System.out.println("sucessfully await");
		}catch(Exception e){
			System.out.println(e.getMessage());
			e.getStackTrace();
			return;
		}
		//Thread curr_thread = Thread.currentThread();
		/*try{
		//Thread.sleep(360000);
		//System.out.printf(threadName+" defaultMetric is %b",AwsSdkMetrics.isDefaultMetricsEnabled());

		}catch(InterruptedException e){
			System.out.println("sleep exception");
		}*/
		/*if(threadName.equals("Thread-2")){
			System.out.println(threadName+" shutdown");
			dynamoDB_inst.dynamoDB.shutdown();
		}*/
		//following is the code to interact with database
		long startTime = System.currentTimeMillis();
		/*Table table = dynamoDB_inst.dynamoDB.getTable("Test2");
		HashMap<String, String> nameMap = new HashMap<String,String>();
		HashMap<String, Object> valueMap = new HashMap<String,Object>();
		String key1 = dynamoDB_inst.operationBi("email", Op.eq, "zero064@gmail.com", Type.Str, nameMap, valueMap);
		String key2 = dynamoDB_inst.operationBi("id", Op.eq, 0, Type.Int, nameMap, valueMap);
		ItemCollection<QueryOutcome> results = null;
	    Iterator<Item> iterator = null;
		Item item = null;
		QuerySpec querySpec = new QuerySpec();
		System.out.println(key1);
		System.out.println(key2);
		querySpec.withKeyConditionExpression(key1+" and "+key2);
		querySpec.withNameMap(nameMap);*/
		Iterator<Item> iterator = job.iterator();
		int[] int_arry = new int[1];
		System.out.println(threadName);
		printTime(date);
		setTargetTime(date,int_arry);
		
		BigDecimal piv_num = new BigDecimal(0);
		Integer hashkey = date.get(Calendar.DAY_OF_MONTH);
		String sortKey = toSortKey(date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE)) ;
			//iterate through all APNs in job
			//Thie item is from APNs table
		System.out.println("before get in while loop");
			while(iterator.hasNext()){
				Item item = iterator.next();
				String email = (String) item.get("email");
				String firstname = (String) item.get("firstname");
				String token = (String) item.get("token");
				String lastname = (String) item.get("lastname");
				String tabName = appointTabName(email);
				ItemCollection<QueryOutcome> results = dynamoDB_inst.Query(tabName,hashkey,sortKey, Op.eq);
				Iterator<Item> it_app = results.iterator();
				int count = results.getAccumulatedItemCount();
				if(count==0){
					//something wrong in the table , send request to recreate the whole table
				}
				while(it_app.hasNext()){
					Item app_queue = it_app.next();
					BigDecimal open = (BigDecimal) app_queue.get("open");
					BigDecimal reserv = (BigDecimal) app_queue.get("reservation");
					List<String> queue = (List<String>) app_queue.get("queue");
					System.out.println("before get in compareTo");
					if(reserv.compareTo(piv_num)>0){
						String message = threadName+":"+firstname+" "+lastname+",you have appoinments in next "+ new Integer(int_arry[0]).toString()+" minutes";
						System.out.println(threadName+":"+message);
						System.out.println("token:"+token);
						try{
							sendNotification(token, apnsClient,message);
						}catch(Exception exception){
							System.out.println(exception.getMessage());
							exception.getStackTrace();
						}
					}
				}
			}
			/*for (int i=1;i<=1500;i++) {
				//valueMap.put(":I", i);
				//querySpec.withValueMap(valueMap);
				ItemCollection<QueryOutcome> results = dynamoDB_inst.Query("Test2","zero064@gmail.com",i, Op.eq);
				Iterator<Item> iterator = results.iterator();
				while(iterator.hasNext()){
					item = iterator.next();
					DateFormat dFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
					TimeZone tZone = TimeZone.getTimeZone("UTC");
					Calendar date = Calendar.getInstance(tZone);
					System.out.printf("Zone ID is %s\n",tZone.getID());
					Date date2 = new Date();
					System.out.println(dFormat.format(date.getTime()));
					printTime(date);
					System.out.println("after change the date");
					date.add(Calendar.MINUTE, 20160);
					printTime(date);
					System.out.println();
					System.out.println(date2);
					BigDecimal num = (BigDecimal) item.get("id");
					System.out.printf(threadName+" id: %s\ntime: %d\n", num.toString(),System.currentTimeMillis()-startTime);
					System.out.printf(threadName+" "+curr_thread.getName()+" "+curr_thread.getId()+"'s alive status is %b\n",curr_thread.isAlive());
					logger.info(threadName+" logging!");
				}
			}*/
			
			
			

		
		
		
	}
	private String toSortKey(int hour, int min){
		String hString = new Integer(hour).toString();
		String mString = new Integer(min).toString();
		if(hour<10) hString = "0"+hString;
		if(min<10) mString = "0"+mString;
		return hString+":"+mString;
	}
	private String appointTabName(String email){
		String extract = "";
		final int length = email.length();
		for(int i=0;i<length;i++){
			if(email.charAt(i) != '@'){
				extract += email.charAt(i);
			}
		}
		return extract;
	}
	private void sendNotification(String token,final ApnsClient<SimpleApnsPushNotification> apnsClient,String message) throws Exception {


		final SimpleApnsPushNotification pushNotification;
		{
		    final ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();
		    payloadBuilder.setAlertBody(message);
		    payloadBuilder.setBadgeNumber(1);
		    payloadBuilder.setSoundFileName("default");

		    final String payload = payloadBuilder.buildWithDefaultMaximumLength();
		    //final String token2 = token;
		    pushNotification = new SimpleApnsPushNotification(token, "com.Berbi.BerbiHealth", payload);
		}
		Future<PushNotificationResponse<SimpleApnsPushNotification>> sendNotificationFuture =
		        apnsClient.sendNotification(pushNotification);
		boolean flag =false;
		while(!flag){
			try {
			    final PushNotificationResponse<SimpleApnsPushNotification> pushNotificationResponse =
			            sendNotificationFuture.get();
	
			    if (pushNotificationResponse.isAccepted()) {
			    	flag = true;
			        System.out.println("Push notitification accepted by APNs gateway.");
			    } else {
			        System.out.println("Notification rejected by the APNs gateway: " +
			                pushNotificationResponse.getRejectionReason());
	
			        if (pushNotificationResponse.getTokenInvalidationTimestamp() != null) {
			            System.out.println("\t…and the token is invalid as of " +
			                pushNotificationResponse.getTokenInvalidationTimestamp());
			        }
			    }
			} catch (final ExecutionException e) {
			    System.err.println("Failed to send push notification.");
			    e.printStackTrace();
	
			    if (e.getCause() instanceof ClientNotConnectedException) {
			        System.out.println("Waiting for client to reconnect…");
			        apnsClient.getReconnectionFuture().await();
			        System.out.println("Reconnected.");
			        sendNotificationFuture =
					        apnsClient.sendNotification(pushNotification);
			    }
			}
			//Thread.sleep(2000);
		}
	}
	private void printTime(Calendar cal){
		System.out.println(cal.getTimeZone());
		System.out.println(cal.get(Calendar.YEAR));
		System.out.println(cal.get(Calendar.MONTH)+1);
		System.out.println(cal.get(Calendar.DAY_OF_MONTH));
		System.out.println(cal.get(Calendar.HOUR_OF_DAY));
		System.out.println(cal.get(Calendar.MINUTE));
	}
	private void setTargetTime(Calendar date,int[] left_time){
		int hour = date.get(Calendar.HOUR_OF_DAY);
		int min = date.get(Calendar.MINUTE);
		if(min>=30){
			left_time[0] = 60-min;
			date.add(Calendar.MINUTE, 60-min);
		}else{
			left_time[0] = 30-min;
			date.add(Calendar.MINUTE,30-min);
		}
		return;
	
	}
	public void startThread(){
		
		System.out.println("Starting "+threadName);
		((Thread) this).start();
		/*if(t==null){
			t = new Thread(this,threadName);
			t.start();
		}*/
	}
	
}
