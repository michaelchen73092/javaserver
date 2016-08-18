package com.BoBiHealth;


import java.util.*;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;

import bolts.Task;

public class  AsyncIteratorScan<T extends ItemV2> implements Iterator<T>{
	private Iterator<T> ite;
	private Map<String,AttributeValue> lastEvaluateKey;
	private ScanRequest scanRequest;
	private Collection<T> collection;
	public AsyncIteratorScan(Collection<T> collection,ScanRequest scanRequest,Map<String, AttributeValue> lastKey) {
		this.collection = new HashSet<T>();
		this.ite = collection.iterator();
		System.out.printf("In iterator, collection size is %d\n, hasNext(): %b\n", this.collection.size(),this.ite.hasNext());
		this.scanRequest = scanRequest;
		this.lastEvaluateKey = lastKey;
	}
	
	public boolean hasNext()
	{
		System.out.printf("In iterator hasNext(), collection size:%d\n",this.collection.size());
		if(ite.hasNext()){ 
			System.out.println("ite.hasNext()");
			return true;
		
		}
		if(!this.lastEvaluateKey.isEmpty()){
			System.out.println("Something left to scan");

			Task<Object> task = dynamoDBManager.instance().scanAsyncNexPage(scanRequest,(Collection<ItemV2>)this.collection, this.lastEvaluateKey);
			try{
			task.waitForCompletion();
			}catch(InterruptedException exception){
				System.out.println(exception.getMessage());
				return false;
			}
			this.ite = this.collection.iterator();
			
			return this.ite.hasNext();
		}else{
			return false;
		}
	}
	public T next(){
		return this.ite.next();
	}
	public void remove(){
		this.ite.remove();
	}
}