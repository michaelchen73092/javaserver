package com.BoBiHealth;

import java.math.BigDecimal;
import java.util.*;

import bolts.Continuation;
import bolts.Task;

public class checkingManager extends TimerTask {
	
	private ArrayList<appointDelegate[]> appointQueue;
	public void run(){
		
		dynamoDBManager dynamoDB_inst = dynamoDBManager.instance();
		Iterator<appointDelegate[]> iterator = appointQueue.iterator();
		Calendar date = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		int[] int_arry = new int[1];
		printTime(date);
		setTargetTime(date,int_arry);
		final BigDecimal piv_num = new BigDecimal(0);
		Integer hashkey = date.get(Calendar.DAY_OF_MONTH);
		String sortKey = toSortKey(date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE)) ;
		final BigDecimal piv_month = new BigDecimal(date.get(Calendar.MONTH)+1);
		final BigDecimal piv_year = new BigDecimal(date.get(Calendar.YEAR));
		//iterate through all APNs in job
			//Thie item is from APNs table
		System.out.println("before get in while loop");
			while(iterator.hasNext()){
				final appointDelegate[] appoint = iterator.next();
				String email = appoint[0].getEmail();
				String tabName = appointTabName(email);
				Task<CollectionWrapper<ItemV2>> task = dynamoDB_inst.Query(tabName,hashkey,sortKey, Op.eq);
				task.continueWith(new Continuation<CollectionWrapper<ItemV2>, Void>(){
					public Void then(Task<CollectionWrapper<ItemV2>> task) throws Exception{
						if(task.isCancelled()){
							
						}else if(task.isFaulted()){
							Exception exception = task.getError();
							throw exception;
						}else{
							checkAppoint(appoint,task.getResult(), piv_year, piv_month, piv_num);
						}
						return null;
					}
				});
			}
			
	}
	private void checkAppoint(appointDelegate[] appoint,CollectionWrapper<ItemV2> collection,BigDecimal piv_year,BigDecimal piv_month,BigDecimal piv_num){
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
				synchronized (appoint) {
					appoint[0].addAppointToQueue(app_queue);

				}
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
	public checkingManager(ArrayList<appointDelegate[]> queue){
		synchronized (queue) {
			this.appointQueue = new ArrayList<appointDelegate[]>(queue);

		}
	}
}
