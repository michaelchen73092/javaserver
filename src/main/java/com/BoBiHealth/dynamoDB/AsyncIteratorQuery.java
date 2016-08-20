package com.BoBiHealth.dynamoDB;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;

import bolts.Task;

public class  AsyncIteratorQuery<T extends ItemV2> implements Iterator<T>{
	private Iterator<T> ite;
	private Map<String,AttributeValue> lastEvaluateKey;
	private QueryRequest queryRequest;
	private Collection<T> collection;
	public AsyncIteratorQuery(Collection<T> collection,QueryRequest queryRequest,Map<String, AttributeValue> lastKey) {
		this.collection = new HashSet<T>();
		this.ite = collection.iterator();
		this.queryRequest = queryRequest;
		this.lastEvaluateKey = lastKey;
	}
	public boolean hasNext()
	{
		if(ite.hasNext()) return true;
		if(!this.lastEvaluateKey.isEmpty()){
			Task<Object> task = dynamoDBManager.instance().queryAsyncNexPage(queryRequest,(Collection<ItemV2>)this.collection, this.lastEvaluateKey);
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