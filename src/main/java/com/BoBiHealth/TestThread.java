package com.BoBiHealth;
import com.amazonaws.metrics.AwsSdkMetrics;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProvider;

import com.relayrides.pushy.apns.ApnsClient;
import com.relayrides.pushy.apns.ClientNotConnectedException;
import com.relayrides.pushy.apns.PushNotificationResponse;
import com.relayrides.pushy.apns.util.ApnsPayloadBuilder;
import com.relayrides.pushy.apns.util.SimpleApnsPushNotification;
import com.relayrides.pushy.apns.util.TokenUtil;
import io.netty.util.concurrent.Future;


import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;


public class TestThread {
	public static void main(String args[])throws Exception{
		//AwsSdkMetrics.setCredentialProvider(new ProfileCredentialsProvider("H:\\AWSCredentials.properties","default"));
		//AwsSdkMetrics.setMetricNameSpace("TestMetric");
		//AwsSdkMetrics.setRegion(com.amazonaws.regions.Regions.US_EAST_1);
		//System.out.printf(" before defaultMetric is %b\n",AwsSdkMetrics.isMetricsEnabled());

		//System.out.printf("triiger is %b\n",AwsSdkMetrics.enableDefaultMetrics());
		//System.out.println(AwsSdkMetrics.CLOUDWATCH_REGION); 
		//System.out.printf(" defaultMetric is %b\n",AwsSdkMetrics.isMetricsEnabled());
		Item item = null;
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
		System.out.println("the end");

		/*Thread.sleep(20000);
		System.out.println("test alive");
		threadManager.instance().stopThread();
		Thread.sleep(1000);
		threadManager.instance().isAlive();*/


	}
	public static void testThread(HashSet<Item> job)throws Exception{
		threadManager inst = threadManager.instance();
		ArrayList<String> threadNames = new ArrayList<String>();
		threadNames.add(inst.initNewThread(job));
		threadNames.add(inst.initNewThread(job));
		inst.startThread();
		//threadManager.instance().isAlive();;

	
		return;
	}
}
