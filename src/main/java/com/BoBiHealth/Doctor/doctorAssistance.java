package com.BoBiHealth.Doctor;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.json.*;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.relayrides.pushy.apns.ApnsClient;
import com.relayrides.pushy.apns.ClientNotConnectedException;
import com.relayrides.pushy.apns.PushNotificationResponse;
import com.relayrides.pushy.apns.util.ApnsPayloadBuilder;
import com.relayrides.pushy.apns.util.SimpleApnsPushNotification;

import java.util.*;
import bolts.*;
import io.netty.util.concurrent.Future;
import com.BoBiHealth.dynamoDB.*;
public class doctorAssistance extends Thread implements appointDelegate,doctorStatusDelegate{
	public static HashMap<String, doctorAssistance[]> assistanceMap = new HashMap<String, doctorAssistance[]>();
	private ArrayList<JSONObject> request_queue; 
	private boolean[] que_lock = new boolean[1] ;
	public static Integer testInt = new Integer(3);
	private doctorAssistance[] access;
	private onWorking[] worker;
	private boolean[] is_online;
	private int[] task_count;
	private int[] appoint_task_count;
	private JSONObject doctor;
	private boolean[] taken;
	public void run(){
		while(true){
			JSONObject curr_request;
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
	public static void testArray(ArrayList<String> arry){
		for(int i=0;i<20;i++){
			arry.add(new Integer(i).toString());
		}
	}
	public static void main(String args[])throws Exception{
		doctorAssistance assistance = new doctorAssistance(new JSONObject(),new doctorAssistance[1]);
		
		assistance.start();
		
		Thread.sleep(40000);
		assistance.interrupt();
		synchronized (assistance.testInt) {
			System.out.println("make it!!");
		}
		ArrayList<String> arrayList = new ArrayList<String>();
		testArray(arrayList);
		for(String str: arrayList){
			System.out.println(str);
		}
		
	}
	
	public doctorAssistance(JSONObject doctor,doctorAssistance[] pipe){
		request_queue = new ArrayList<JSONObject>();
		worker = new onWorking[1];
		this.access = pipe;
		this.doctor = doctor;
		this.que_lock[0] = false;
		this.taken = new boolean[1];
		task_count = new int[1];
		task_count[0] = 0;
		appoint_task_count = new int[1];
		appoint_task_count[0] = 0;
		is_online = new boolean[1];
		is_online[0] = false;
	}
	public boolean hasTask(){
		synchronized (task_count) {
			if(task_count[0]>0){
				return true;
			}else{
				return false;
			}
		}
	}
	public void addAppointToQueue(ItemV2 item){
		synchronized (request_queue) {
			request_queue.add(item.getJSONObject());
			synchronized (worker) {
				if(worker[0] == null){
					worker[0] = new onWorking(this.request_queue,this,this.doctor);
					worker[0].start();
					addTask();
				}
			}

		}
		removeAppointTask();
		removeTask();
		return;
	}
	private void addAppointTask(){
		if(appoint_task_count[0]==0){
			addToCheckingManager();
		}
		appoint_task_count[0]++;
	}
	private void removeAppointTask(){
		appoint_task_count[0]--;
		if(appoint_task_count[0]==0){
			removeFromAppointChecking();
		}
		
	}
	private void addToCheckingManager(){
		
	}
	private void removeFromAppointChecking(){
		
	}
	private void addTask(){
		synchronized (task_count) {
			task_count[0]++;
		}
	}
	private void removeTask(){
		boolean suggestion = false;
		synchronized (task_count) {
			task_count[0]--;
			if(task_count[0]==0){
				suggestion = true;
			}
		}

		if(suggestion) shutDownMe();
	}
	private void shutDownMe(){
		
	}
	public void modifyHeartBeat(BigInteger hearbeat){
		synchronized (worker) {
			worker[0].modifyHeartBeat(hearbeat);
		}
	}
	//for online doctor 
	//true: success, false: fail
	public boolean get_in_OnlineQueue(JSONObject patient){
		synchronized (request_queue) {
			synchronized (is_online) {
				if(is_online[0]){
					request_queue.add(patient);
					return true;
				}else{
					return false;
				}
			}
		}
	}
	//delegate's job
	//inform worker, the patient is handled by doctor
	public String getEmail(){
		return this.doctor.getString("email");
	}
	public boolean taken(){
		synchronized (this.taken) {
			if(this.taken[0]){
				this.taken[0] = false;
				return true;
			}
		}
		return false;
	}
	public void take(){
		synchronized (this.taken) {
			this.taken[0] = true;
		}
	}
	public void turnOffline()throws InterruptedException{
		synchronized (worker) {
			synchronized (is_online) {
				String hashkey = this.doctor.getString("email"); 
		    	final ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();
			    if(is_online[0]){
			    	payloadBuilder.setAlertBody(worker[0].message.informDoctorOutConnectionOffline());
			    }else{
			    	payloadBuilder.setAlertBody(worker[0].message.informDoctorOutConnection());
			    }
		    	payloadBuilder.setBadgeNumber(1);
			    payloadBuilder.setSoundFileName("default");
			    final String payload = payloadBuilder.buildWithDefaultMaximumLength();
			    worker[0].fetchAPNsAndsendNotification(this.doctor.getString("email"), payload);
				is_online[0] = false;
			}
			removeTask();
			worker[0] = null;
		}

	}
	public void lockQueue(){
		synchronized (que_lock) {
			que_lock[0] = true;
		}		
		return;
	}
	public void unlockQueue(){
		synchronized (que_lock) {
			que_lock[0] = false;
		}
		return;
	}
}
class onWorking extends Thread{
	private ArrayList<JSONObject> request_queue; 
	private JSONObject current_patient;
	private JSONObject doctor;
	private ApnsClient<SimpleApnsPushNotification> apnsClient;
	public messageGenerator message;
	private dynamoDBManager dynamoDB_inst;
	private doctorStatusDelegate supervisor;
	private BigInteger[] hearBeat;
	private boolean[] call_is_end;

	public void run(){
		while(true){
			try{
				checkQueue();
				boolean inform_success = informDoctor();
				if(inform_success){
					if(!checkHearBeat()){
						expellQueue();
						return;
					}
				}else{
					expellQueue();
					return;
				}
			}catch(InterruptedException exception){
				return;
			}
		}

	}
	private boolean checkQueue()throws InterruptedException{
		while(true){
			synchronized (request_queue) {
				if(request_queue.size() > 0 ){
					current_patient = request_queue.remove(0);
				}
			}
			if(current_patient == null){
				Thread.sleep(60000);

			}else{
				return true;
			}
			
		}
	}
	public void modifyHeartBeat(BigInteger hearbeat){
		synchronized (this.hearBeat) {
			this.hearBeat[0] = hearbeat;
		}
	}
	private boolean checkHearBeat()throws InterruptedException{
		int period_limit = 20*30;
		while(true){
			int count_end = 0;
			while(count_end++ < period_limit){
				synchronized (this.call_is_end) {
					if(this.call_is_end[0]){
						this.call_is_end[0] = false;
						return true;
					}
				}
				Thread.sleep(50);
			}
			synchronized (this.hearBeat) {
				Calendar date = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
				long recorded_hearbeat = this.hearBeat[0].longValue();
				if((date.getTimeInMillis()-recorded_hearbeat)> 300000){
					return false;
				}
			}
		}
	}
	private boolean informDoctor()throws InterruptedException{
		int count = 0;
		//how long to send the next push notification
		int taken_check = 5*180;
		String hashkey = doctor.getString("email");
		String email = (String) doctor.getString("email");
		String firstname = (String) doctor.get("firstname");
		String lastname = (String) doctor.get("lastname");
		//build message and APNs message's body
	    
    	final ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();
	    payloadBuilder.setAlertBody(message.informDoctor());
	    payloadBuilder.setBadgeNumber(1);
	    payloadBuilder.setSoundFileName("default");
	    final String payload = payloadBuilder.buildWithDefaultMaximumLength();
	    

		// = dynamoDB_inst.Query(tabName,hashkey,sortKey, Op.eq);
		Task<CollectionWrapper<ItemV2>> task;
		
		while(count++ <7){
			int count_taken = 0;
			fetchAPNsAndsendNotification(hashkey, payload);
			while(count_taken++ < taken_check){
				Thread.sleep(200);
				if(supervisor.taken()){
					return true;
				}
			}

		}
		return false;
	}
	public  void fetchAPNsAndsendNotification(String hashkey,String payload) throws InterruptedException{
		boolean flag = false;
		String token;
		ItemV2 contact;
		Iterator<ItemV2> target_que = null;
		Task<CollectionWrapper<ItemV2>> task;

		while(!flag){
			task = dynamoDB_inst.Query("APNs",hashkey,null, Op.eq);
			try{
				task.waitForCompletion();
				flag = true;
				target_que = task.getResult().iterator();

			}catch(Exception exception){
				System.out.println(exception.getMessage());
			}
			
				Thread.sleep(10);
	
		}
		while(target_que.hasNext()){
			contact = target_que.next();
			token = (String) contact.get("token");
			sendNotification(token, this.apnsClient,payload);

		}
	}
	private void sendNotification(String token,final ApnsClient<SimpleApnsPushNotification> apnsClient,String payload) throws InterruptedException {


		final SimpleApnsPushNotification pushNotification;
		{
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
			} catch (final Exception e) {
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
	//expell all patien in the queue
	public void expellQueue()throws InterruptedException{
		supervisor.lockQueue();
		JSONObject nextOne = current_patient;
		this.current_patient = null;

	    String hashkey;

		// = dynamoDB_inst.Query(tabName,hashkey,sortKey, Op.eq);
		Task<CollectionWrapper<ItemV2>> task;
		synchronized (request_queue) {
			supervisor.turnOffline();
			do{
				hashkey = nextOne.getString("email"); 
		    	final ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();
			    payloadBuilder.setAlertBody(message.informPatienLeave(nextOne.getString("firstname"), nextOne.getString("lastname")));
			    payloadBuilder.setBadgeNumber(1);
			    payloadBuilder.setSoundFileName("default");
			    final String payload = payloadBuilder.buildWithDefaultMaximumLength();
				try{
					fetchAPNsAndsendNotification(hashkey, payload);
				}catch(InterruptedException exception){
					
				}
				finally{
					nextOne = request_queue.size()>0 ? request_queue.remove(0):null;
				}
			}while(nextOne != null);
		}
	}
	public onWorking(ArrayList<JSONObject> request_queue,doctorStatusDelegate boss,JSONObject doctor){
		this.request_queue = request_queue;
		this.doctor = doctor;
		this.supervisor = boss;
		this.call_is_end = new boolean[1];
		this.message = new messageGenerator(true, doctor);
		this.hearBeat = new BigInteger[1];
		dynamoDBManager dynamoDB_inst = dynamoDBManager.instance();
		try{
		this.apnsClient = new ApnsClient<SimpleApnsPushNotification>(
		        new File("H:\\Certificate\\Certificate\\Certificates.p12"),"Cc5302196029");
		final Future<Void> connectFuture = apnsClient.connect(ApnsClient.DEVELOPMENT_APNS_HOST);
		connectFuture.await();
		System.out.println("sucessfully await");
		}catch(Exception e){
			System.out.println(e.getMessage());
			e.getStackTrace();
			return;
		}
	}
}
