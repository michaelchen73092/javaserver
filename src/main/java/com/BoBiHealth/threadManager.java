package com.BoBiHealth;
import java.util.*;
import com.amazonaws.services.dynamodbv2.document.*;
import com.BoBiHealth.dynamoDB.*;
public class threadManager {
	private static threadManager inst = null;
	private HashMap<String,dynamoDBthread> threadList = new HashMap<String,dynamoDBthread>();
	private HashSet<Integer> keySet = new HashSet<Integer>();
	public Integer threadNum(){
		return threadList.size();
	}
	//this function create a new thread and return the thread the new thread's name
	public String initNewThread(Collection<ItemV2> job){
		String threadName = null;
		Integer i = 0;
		for(i=0;keySet.contains(i);i++);
		keySet.add(i);
		threadName = "Thread-"+i.toString();
		assert(!threadList.containsKey(threadName));
		dynamoDBthread newThread = new dynamoDBthread(threadName,job,Calendar.getInstance(TimeZone.getTimeZone("UTC")));
		System.out.printf("Start "+threadName+" "+newThread.getName()+" "+newThread.getId()+"\n");
		threadList.put(threadName,newThread);
		return threadName;
	}
	public void startThread(){
		Iterator<String> iterator = threadList.keySet().iterator();
		while(iterator.hasNext()){
			threadList.get(iterator.next()).startThread();;
		}
		return;
	}
	public void stopThread() {
		Iterator<String> iterator = threadList.keySet().iterator();
		String threadName = null;
		while(iterator.hasNext()){
			try{
				threadName = iterator.next();
				threadList.get(threadName).interrupt();;
				System.out.println(threadName+" is interrupted");
			}catch(SecurityException e){
				System.out.println(threadName+" "+e.getMessage());
			}
		}
		return;
	}
	public void isAlive() throws Exception{
		Iterator<String> iterator = threadList.keySet().iterator();
		String threadName = null;
		boolean alive = false;
		Thread temp = null;
		String errorMessage = "\n";
		while(iterator.hasNext()){
			threadName = iterator.next();
			temp = threadList.get(threadName);
			System.out.printf("in test alive "+threadName+" "+temp.getName()+" "+temp.getId()+" is %b\n",temp.isAlive());
			if(temp.isAlive()){
				alive = true;
				errorMessage += threadName+" is still alive!!\n"; 
			}
		}
		threadList.clear();
		keySet.clear();
		if(alive){
			Exception exception = new Exception(errorMessage);
			exception.getStackTrace();
			throw exception;
		}
		return;
		
	}
	public static threadManager instance(){
		if(inst==null){
			inst = new threadManager();
		}
		return inst;
	}
}
