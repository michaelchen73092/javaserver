package com.BoBiHealth;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.relayrides.pushy.apns.ApnsClient;
import com.relayrides.pushy.apns.ClientNotConnectedException;
import com.relayrides.pushy.apns.PushNotificationResponse;
import com.relayrides.pushy.apns.util.ApnsPayloadBuilder;
import com.relayrides.pushy.apns.util.SimpleApnsPushNotification;

import io.netty.util.concurrent.Future;

public class pushView extends TimerTask {
	private String threadName;
	private Calendar date;
    private static final Logger logger = LogManager.getLogger(dynamoDBthread.class);
	pushView(String name,Calendar date){
		this.threadName = name;
		this.date = date;
		System.out.println("Creating "+threadName);
	}
	public void run(){
		//System.out.printf(threadName+" defaultMetric is %b\n",AwsSdkMetrics.isMetricsEnabled());
		//System.out.println("Running "+threadName);
		ApnsClient<SimpleApnsPushNotification> apnsClient;
		try{
		apnsClient = new ApnsClient<SimpleApnsPushNotification>(
		        new File("H:\\Certificate\\Certificate\\CertificatesBerbi.p12"),"Cc5302196029!!");
		final Future<Void> connectFuture = apnsClient.connect(ApnsClient.DEVELOPMENT_APNS_HOST);
		connectFuture.await();
		System.out.println("sucessfully await");
		}catch(Exception e){
			System.out.println(e.getMessage());
			e.getStackTrace();
			return;
		}

		long startTime = System.currentTimeMillis();
		int[] int_arry = new int[1];
		System.out.println(threadName);
		printTime(date);
		setTargetTime(date,int_arry);
		
		BigDecimal piv_num = new BigDecimal(0);
		Integer hashkey = date.get(Calendar.DAY_OF_MONTH);
		String sortKey = toSortKey(date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE)) ;
		BigDecimal piv_month = new BigDecimal(date.get(Calendar.MONTH)+1);
		BigDecimal piv_year = new BigDecimal(date.get(Calendar.YEAR));
		//iterate through all APNs in job
			//Thie item is from APNs table
		System.out.println("before get in while loop");
			
				String firstname = "Wei-Chih";
				String token = "da5322ac50f098b09e4731d0376da38e0c32194130d6eb185ed6a9bf00066038";
				String lastname = "Chen";


					//System.out.println("before get in compareTo");
	
					if(true){
						String message = threadName+":"+firstname+" "+lastname+",you have appoinments in next  minutes";
						System.out.println(threadName+":"+message);
						System.out.println("token:"+token);
						try{
							sendNotification(token, apnsClient,message);
						}catch(Exception exception){
							System.out.println(exception.getMessage());
							exception.getStackTrace();
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
			return;
			
			

		
		
		
	}
	public static void main(String args[]){
		Timer timer = new Timer(false);
		timer.schedule(new pushView("thread1-", Calendar.getInstance(TimeZone.getTimeZone("UTC"))), 20, 20000);
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
		    pushNotification = new SimpleApnsPushNotification(token, "com.Berbi.Berbi", payload);
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
	
}
