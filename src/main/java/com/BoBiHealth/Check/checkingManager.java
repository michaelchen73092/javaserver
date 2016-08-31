package com.BoBiHealth.Check;
import java.math.BigDecimal;
import java.util.*;

import bolts.*;
import com.BoBiHealth.Doctor.*;
import com.BoBiHealth.dynamoDB.*;
public class checkingManager extends Thread {
	public static Collection<Long> track_set = new HashSet<Long>();
	private Collection<appointDelegate[]> appointQueue;
	public void run(){
		
		dynamoDBManager dynamoDB_inst = dynamoDBManager.instance();
		Iterator<appointDelegate[]> iterator = appointQueue.iterator();
		final Calendar date = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		int[] int_arry = new int[1];
		Collection<Task<Object>> task_collection = new ArrayList<Task<Object>>();
		printTime(date);
		setTargetTime(date,int_arry);
		final BigDecimal piv_num = new BigDecimal(0);
		Integer hashkey = date.get(Calendar.DAY_OF_MONTH);
		String sortKey = toSortKey(date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE)) ;
		final BigDecimal piv_month = new BigDecimal(date.get(Calendar.MONTH)+1);
		final BigDecimal piv_year = new BigDecimal(date.get(Calendar.YEAR));
		final AppointCollector collector = new AppointCollector();
		//iterate through all APNs in job
			//Thie item is from APNs table
		System.out.println("before get in while loop");
			while(iterator.hasNext()){
				final appointDelegate[] appoint = iterator.next();
				String email = appoint[0].getEmail();
				String tabName = appointTabName(email);
				Task<CollectionWrapper<ItemV2>> task = dynamoDB_inst.Query(tabName,hashkey,sortKey, Op.eq);
				final TaskCompletionSource<Object> taskCompletionSource = new TaskCompletionSource<Object>();
				task_collection.add(taskCompletionSource.getTask());
				task.continueWithTask(new Continuation<CollectionWrapper<ItemV2>, Task<Object>>(){
					public Task<Object> then(Task<CollectionWrapper<ItemV2>> task) throws Exception{
						if(task.isCancelled()){
							taskCompletionSource.setCancelled();
							return null;
							
						}else if(task.isFaulted()){
							Exception exception = task.getError();
							System.out.println(exception.getMessage());
							taskCompletionSource.setError(exception);
							throw exception;
						}else{
							checkAppoint(appoint,task.getResult(), piv_year, piv_month, piv_num,collector);
							taskCompletionSource.setResult(null);
						}
						return null;
					}
				});
			}
			Task<Void> final_task = Task.whenAll(task_collection);
			try{
				final_task.waitForCompletion();
			}catch(Exception exception){
				return;
			}
			
			Timer timer = new Timer();
			long delay = date.getTimeInMillis()-System.currentTimeMillis();
			Long timestamp = new Long(date.getTimeInMillis());
			synchronized (checkingManager.track_set) {
				if(checkingManager.track_set.contains(timestamp)){
					
				}else{
					checkingManager.track_set.clear();
					timer.schedule(collector, delay>0 ? delay:2);
					checkingManager.track_set.add(timestamp);
				}
			}

			
	}
	private void checkAppoint(appointDelegate[] appoint,CollectionWrapper<ItemV2> collection,BigDecimal piv_year,BigDecimal piv_month,BigDecimal piv_num,AppointCollector collector){
		Iterator<ItemV2> it_app = collection.iterator();
		int count = collection.size();
		if(count==0){
			//something wrong in the table , send request to recreate the whole table
			System.out.println("wrong table please remake");
		}
		while(it_app.hasNext()){
			ItemV2 app_queue = it_app.next();
			BigDecimal open = (BigDecimal) app_queue.get("open");
			BigDecimal reserv = (BigDecimal) app_queue.get("reservation");
			BigDecimal month =(BigDecimal) app_queue.get("month");
			BigDecimal year = (BigDecimal) app_queue.get("year");
			List<String> queue = (List<String>) app_queue.get("queue");
			//System.out.println("before get in compareTo");
			if(year.compareTo(piv_year)!=0 || month.compareTo(piv_month)!=0){
				System.out.println("Incorrect Month year");
				System.out.printf("Suppose year:%s month:%s\n",piv_year.toString(),piv_month.toString());
				System.out.printf("Recorded year:%s month:%s\n",year.toString(),month.toString());
				//issue request to another server to rewrite the entry
				break;
			}
			if(reserv.compareTo(piv_num)>0){
				collector.addAppoint(appoint, app_queue);					
			}
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
	private String toSortKey(int hour, int min){
		String hString = new Integer(hour).toString();
		String mString = new Integer(min).toString();
		if(hour<10) hString = "0"+hString;
		if(min<10) mString = "0"+mString;
		return hString+":"+mString;
	}
	public static String appointTabName(String email){
		String extract = "";
		final int length = email.length();
		for(int i=0;i<length;i++){
			if(email.charAt(i) != '@'){
				extract += email.charAt(i);
			}
		}
		return extract;
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
	public checkingManager(Collection<appointDelegate[]> queue){
		synchronized (queue) {
			this.appointQueue = new HashSet<appointDelegate[]>(queue);

		}
	}
}
class AppointCollector extends TimerTask{
	HashMap<appointDelegate[], ItemV2> map;
	public void run(){
		Iterator<appointDelegate[]> ite = map.keySet().iterator();
		while(ite.hasNext()){
			appointDelegate[] pipe = ite.next();
			ItemV2 appont_queue = map.get(pipe);
			pipe[0].addAppointToQueue(appont_queue);
		}
	}
	public void AppointCollector(){
		map = new HashMap<appointDelegate[],ItemV2>();
	}
	public void addAppoint(appointDelegate[] delegate,ItemV2 item){
		synchronized (map) {
			map.put(delegate, item);
		}
	}
}