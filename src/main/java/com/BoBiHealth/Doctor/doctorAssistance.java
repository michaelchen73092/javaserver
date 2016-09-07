package com.BoBiHealth.Doctor;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.json.*;
import com.amazonaws.services.dynamodbv2.model.*;
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
import com.BoBiHealth.Check.*;
//is_appoinment in ItemV2 is to used to check whether the patient is appoinment patient
public class doctorAssistance extends Thread implements appointDelegate,doctorStatusDelegate{
	public static HashMap<String, doctorAssistance[]> assistanceMap = new HashMap<String, doctorAssistance[]>();
	private ArrayList<ItemV2> request_queue; 
	private boolean[] que_lock = new boolean[1] ;
	public static Integer testInt = new Integer(3);
	private doctorAssistance[] access;
	//lock set order: worker,is_online
	private onWorking[] worker;
	private boolean[] is_online;
	private int[] task_count;
	private int[] appoint_task_count;
	private ItemV2 doctor;
	private boolean[] taken;
	public void run(){
		while(true){
			ItemV2 curr_request;
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

		ArrayList<String> arrayList = new ArrayList<String>();
		testArray(arrayList);
		for(String str: arrayList){
			System.out.println(str);
		}
		
	}
	
	public doctorAssistance(ItemV2 doctor,doctorAssistance[] pipe){
		request_queue = new ArrayList<ItemV2>();
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
	//item includes a appointment queue.
	//only email and order is reserved in the queue.
	//This function fetch patient information from Persons and add it to queue
	public void addAppointToQueue(ItemV2 item){
		List<JSONArray> queue = (List<JSONArray>) item.get("queue");
		List<ItemV2> buffer_queue = new java.util.ArrayList<>(queue.size());
		List<Task<Object>> task_arry = new java.util.ArrayList<>();
		for(int i=0,length=queue.size();i<length;i++){
			JSONArray arry = queue.get(i);
			Task<CollectionWrapper<ItemV2>> querytask = dynamoDBManager.instance().Query("PersonsPublic",(String)arry.get(1), null, Op.eq);
			TaskCompletionSource<Object> taskCompletionSource = new TaskCompletionSource<>();
			final int i_special = i;
			querytask.continueWith(new Continuation<CollectionWrapper<ItemV2>, Void>(){
				public Void then(Task<CollectionWrapper<ItemV2>> task)throws Exception{
					if(task.isFaulted()){
						taskCompletionSource.setError(task.getError());
					}else{
						Iterator<ItemV2> iterator = task.getResult().iterator();
						while (iterator.hasNext()) {
							ItemV2 patient = iterator.next();
							patient.store("is_appointment", new Boolean(true));
							buffer_queue.add(i_special, patient);;
							
						}
						taskCompletionSource.setResult(null);
					}
					return null;
				}
			});
			task_arry.add(taskCompletionSource.getTask());
		}
		Task<Void> total_task = Task.whenAll(task_arry);
		try{
			total_task.waitForCompletion();
		}catch(InterruptedException exception){
			return;
		}
		synchronized (request_queue) {
			request_queue.addAll(buffer_queue);
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
	public void addAppointTask(){
		if(appoint_task_count[0]==0){
			addToCheckingManager();
		}
		appoint_task_count[0]++;
	}
	public  void removeAppointTask(){
		appoint_task_count[0]--;
		if(appoint_task_count[0]==0){
			removeFromAppointChecking();
		}
		
	}
	private void addToCheckingManager(){
		AppointCheckManager.addAppoint((String)doctor.get("email"));
	}
	private void removeFromAppointChecking(){
		AppointCheckManager.removeAppoint((String)doctor.get("email"));
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
		try{
		PipeManager.instance.Get(Request.doctorAssistance_App.contexName, Request.doctorAssistance_App.Path.shutdown, "doctor="+(String)doctor.get("email"));
		}catch(Exception exception){
			System.out.println(exception.getMessage());
			exception.printStackTrace(System.out);
		}
	}
	public void modifyHeartBeat(BigInteger hearbeat){
		synchronized (worker) {
			worker[0].modifyHeartBeat(hearbeat);
		}
	}
	//for online doctor 
	//true: success, false: fail
	public boolean get_in_OnlineQueue(ItemV2 patient){
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
		return (String)this.doctor.get("email");
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
	public boolean isOnline(){
		synchronized (is_online) {
			return is_online[0];
		}
	}
	//inform the assistant that doctor has started to handled this patient
	public void take(){
		synchronized (this.taken) {
			this.taken[0] = true;
		}
	}
	public void endCall(){
		worker[0].endCall();
	}
	//item's format(information type)
	//turn doctor's status to online, and set the openings number
	//add doctor to onlineDoctors table
	//item-ItemV2 doctor
	//    -....othre attributes
	public void turnOnline(ItemV2 item){
		BigDecimal num = (BigDecimal) item.get("openings");
		synchronized (worker) {
			synchronized (is_online) {
				if(worker[0] == null){
					worker[0] = new onWorking(this.request_queue,this,this.doctor);
					worker[0].start();
					addTask();
					worker[0].modifyOpeings(num);
				}
				is_online[0] = true;
			}
		}
		ItemV2 doctor = (ItemV2)item.get("doctor");
		PutItemRequest putItemRequest = new PutItemRequest();
		putItemRequest.withItem(doctor.toAttributeValueMap());
		putItemRequest.withTableName("onlineDoctors");
		Task<Object> returned_task = dynamoDBManager.instance().putItemAsync(putItemRequest);
		returned_task.continueWith(new Continuation<Object, Void>(){
			public Void then(Task<Object> task) throws Exception{
				if (task.isFaulted()) {
					System.out.println("Error");
				}else{
					
				}
				return null;
			}
		});
	}
	//inform doctor that he turn offline and expell the pointer to worker
	public void turnOffline(boolean endbyDoctor)throws InterruptedException{
		synchronized (worker) {
			synchronized (is_online) {
				String hashkey = (String)this.doctor.get("email"); 
		    	final ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();
			    if(is_online[0]){
			    	if(endbyDoctor){
			    		payloadBuilder.setAlertBody(worker[0].message.endInterview());
			    	}else{
			    		payloadBuilder.setAlertBody(worker[0].message.informDoctorOutConnectionOffline());
			    	}
			    	is_online[0] = false;
			    	DeleteItemRequest request = new DeleteItemRequest();
			    	Map<String, Object> key_map = new HashMap<>();
			    	key_map.put("email",(String)doctor.get("email"));
			    	request.withKey((new ItemV2(key_map)).toAttributeValueMap());
			    	dynamoDBManager.instance().deleteItemAsync(request);
			    }else{
			    	payloadBuilder.setAlertBody(worker[0].message.informDoctorOutConnection());
			    }
		    	payloadBuilder.setBadgeNumber(1);
			    payloadBuilder.setSoundFileName("default");
			    final String payload = payloadBuilder.buildWithDefaultMaximumLength();
			    worker[0].fetchAPNsAndsendNotification((String)this.doctor.get("email"), payload);
			}
			removeTask();
			worker[0] = null;
		}

	}
	public boolean modifyOpenings(BigDecimal num){
		return worker[0].modifyOpeings(num);
	}
	public boolean addOnlinePatient(ItemV2 patient){
		return worker[0].addOnlinePatient(patient);
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
	private ArrayList<ItemV2> request_queue; 
	private ItemV2 current_patient;
	private ItemV2 doctor;
	private ApnsClient<SimpleApnsPushNotification> apnsClient;
	public messageGenerator message;
	private dynamoDBManager dynamoDB_inst;
	private doctorStatusDelegate supervisor;
	private BigInteger[] hearBeat;
	private boolean[] call_is_end;
	private boolean[] endInterview;
	//lock set order:openings, registration
	private int[] openings;
	private int[] registrations;
	public void run(){
		while(true){
			try{
				if(checkQueue()){
					informPatient();
					boolean inform_success = informDoctor();
					if(inform_success){
						if(!checkHearBeat()){
							expellQueue(false);
							return;
						}
					}else{
						boolean endbyDoctor;
						synchronized (endInterview) {
							endbyDoctor = endInterview[0];
						}
						expellQueue(endbyDoctor);
						return;
					}
				}else{
					expellQueue(true);
					return;
				}
			}catch(InterruptedException exception){
				return;
			}
		}

	}
	public void endCall(){
		call_is_end[0] = true;
	}
	//routinely check whether new patient come into the queue
	//if there is, move it to current_patient spot
	//also check whether doctor want to end this interview
	private boolean checkQueue()throws InterruptedException{
		int limit = 60;
		while(true){
			synchronized (request_queue) {
				if(request_queue.size() > 0 ){
					current_patient = request_queue.remove(0);
				}
			}
			if(current_patient == null){
				int count = 0;
				while(count++ < limit){
					synchronized (endInterview) {
						if(endInterview[0]) return false;
					}
					Thread.sleep(1000);
				}

			}else{
				//check whther current_patient is appoinment patient, 
				//if not, decrease registration[0]
				//when registration[0] == (openings[0]-1), put the doctor information to onlineDoctors
				Boolean flag = (Boolean) current_patient.get("is_appointment");
				if(flag.booleanValue()){
					return true;
				}else{
					synchronized (openings) {
						synchronized (registrations) {
							registrations[0]--;
							if(registrations[0] == (openings[0]-1)){
								PutItemRequest putItemRequest = new PutItemRequest();
								putItemRequest.withItem(doctor.toAttributeValueMap());
								putItemRequest.withTableName("onlineDoctors");
								Task<Object> returned_task = dynamoDBManager.instance().putItemAsync(putItemRequest);
								returned_task.continueWith(new Continuation<Object, Void>(){
									public Void then(Task<Object> task) throws Exception{
										if (task.isFaulted()) {
											System.out.println("Error");
										}else{
											
										}
										return null;
									}
								});
								
							}
						}
					}
				}
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
	public void endInterview(){
		synchronized (endInterview) {
			endInterview[0] = true;
		}
		return;
	}
	private void informPatient() throws InterruptedException{
		ItemV2 buffer = current_patient;
		ArrayList<ItemV2> buffer_queue;
		synchronized (request_queue) {
			buffer_queue = new ArrayList<>(request_queue);
		}
		if(supervisor.isOnline()){
			UpdateItemRequest request = new UpdateItemRequest();
			Map<String, Object> key_map = new HashMap<>();
			key_map.put("email", (String)doctor.get("email"));
			Map<String, Object> value_map = new HashMap<>();
			value_map.put("waitinglist_length", new BigDecimal(buffer_queue.size()));
			request.withKey((new ItemV2(key_map)).toAttributeValueMap());
			request.setAttributeUpdates((new ItemV2(value_map)).toAttributeValueUpdate(AttributeAction.PUT));
			request.withTableName("onlineDoctors");
			dynamoDBManager.instance().updateItemAsync(request);
		}
		int count = 0;
		informPatient(buffer,count++);
		for(ItemV2 item:buffer_queue){
			informPatient(item,count++);
		}
		
	}
	private void informPatient(ItemV2 item,int count) throws InterruptedException{
		String hashkey =(String) item.get("email");
		String email = (String) item.get("email");
		//build message and APNs message's body
	    
    	final ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();
	    payloadBuilder.setAlertBody("#4:"+(new Integer(count)).toString());
	    payloadBuilder.setBadgeNumber(1);
	    payloadBuilder.setSoundFileName("default");
	    final String payload = payloadBuilder.buildWithDefaultMaximumLength();
	    

		// = dynamoDB_inst.Query(tabName,hashkey,sortKey, Op.eq);
			fetchAPNsAndsendNotification(hashkey, payload);

	}
	private boolean informDoctor()throws InterruptedException{
		int count = 0;
		//how long to send the next push notification
		int taken_check = 5*180;
		String hashkey =(String) doctor.get("email");
		String email = (String) doctor.get("email");
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
				synchronized (endInterview) {
					if(endInterview[0]) return false;
				}
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
	public boolean modifyOpeings(BigDecimal num){
		synchronized (openings) {
			this.openings[0] = num.intValue();
		}
		return true;
	}
	//add unappointment patient to the queue, the the number of the unappointment reaches the threshold
	//remove doctor from onlineDoctors
	public boolean addOnlinePatient(ItemV2 patient){
		synchronized (openings) {
			synchronized (registrations) {
				if(registrations[0]<openings[0]){
					synchronized (request_queue) {
						patient.store("is_appointment", new Boolean(false));
						request_queue.add(patient);
					}
					registrations[0]++;
					if(registrations[0]==openings[0]){
						DeleteItemRequest deleteItemRequest = new DeleteItemRequest();
						Map<String, Object> key_map = new java.util.HashMap<>();
						key_map.put("email", (String) doctor.get("email"));
						deleteItemRequest.withKey((new ItemV2(key_map)).toAttributeValueMap());
						Task<Object> returned_task = dynamoDBManager.instance().deleteItemAsync(deleteItemRequest);
						returned_task.continueWith(new Continuation<Object, Void>(){
							public Void then(Task<Object> task) throws Exception{
								if(task.isFaulted()){
									System.out.println("Error!!");
								}else{
									
								}
								return null;
							}
						});
					}
					return true;
				}else{
					return false;
				}
			}
		}
	}
	//expell all patien in the queue
	public void expellQueue(boolean endbyDoctor)throws InterruptedException{
		supervisor.lockQueue();
		ItemV2 nextOne = current_patient;
		this.current_patient = null;

	    String hashkey;

		// = dynamoDB_inst.Query(tabName,hashkey,sortKey, Op.eq);
		Task<CollectionWrapper<ItemV2>> task;
		synchronized (request_queue) {
			supervisor.turnOffline(endbyDoctor);
			do{
				hashkey = (String)nextOne.get("email"); 
		    	final ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();

		    	payloadBuilder.setAlertBody(message.informPatienLeave(endbyDoctor,nextOne.getString("firstname"), nextOne.getString("lastname")));

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
	public onWorking(ArrayList<ItemV2> request_queue,doctorStatusDelegate boss,ItemV2 doctor){
		this.request_queue = request_queue;
		this.doctor = doctor;
		this.supervisor = boss;
		this.call_is_end = new boolean[1];
		this.message = new messageGenerator(true, doctor);
		this.hearBeat = new BigInteger[1];
		this.endInterview = new boolean[1];
		this.openings = new int[1];
		this.registrations = new int[1];
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
