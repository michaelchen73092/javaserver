package com.BoBiHealth;
import com.amazonaws.services.dynamodbv2.model.*;
import java.util.*;
import bolts.*;

public class CollectionWrapper<T extends ItemV2> implements Iterable<T>   {
	private Collection<T> collection;
	private Map<String,AttributeValue> lastEvaluateKey;
	private Iterator<T> iterator;
	private String type;
	private QueryRequest queryRequest;
	private ScanRequest scanRequest;
	public CollectionWrapper(QueryRequest queryRequest,Map<String,AttributeValue> lastKey) {
		collection = new HashSet<T>();
		lastEvaluateKey = new HashMap<String, AttributeValue>();
		if(lastKey != null) lastEvaluateKey.putAll(lastKey);
		type = "Query";
		this.queryRequest = queryRequest;
	}
	public CollectionWrapper(ScanRequest scanRequest,Map<String,AttributeValue> lastKey) {
		System.out.println("CollectionWrapper for SCAN");
		collection = new HashSet<T>();
		lastEvaluateKey = new HashMap<String, AttributeValue>();
		if(lastKey != null) lastEvaluateKey.putAll(lastKey);
		type = "Scan";
		this.scanRequest = scanRequest;
	}
	public Iterator<T> iterator(){
		if(type.equals("Query")){
			return new AsyncIteratorQuery<T>(collection, queryRequest, lastEvaluateKey);
		}else if(type.equals("Scan")){
			return new AsyncIteratorScan<T>(collection, scanRequest, lastEvaluateKey);
		}else{
			return null;
		}
	}
	
	public boolean add(T t){
		return collection.add(t);
	}
	public int size(){
		return collection.size();
	}
	/*
	public boolean addAll(Collection<? extends T> t){
		return collection.addAll(t);
	}
	public void clear(){
		collection.clear();
	}
	public boolean contains(Object c){
		return collection.contains(c);
	}
	public boolean containsAll(Collection<?> c){
		return collection.containsAll(c);
	}
	public int hashCode(){
		return collection.hashCode();
	}
	public boolean isEmpty(){
		return collection.isEmpty() && (this.lastEvaluateKey.isEmpty());
	}
	public boolean remove(Object o){
		return collection.remove(o);
	}
	public boolean removeAll(Collection<?> c){
		return collection.removeAll(c);
	}
	public boolean retainAll(Collection<?> c){
		return collection.retainAll(c);
	}
	public Object[] toArray(){
		return collection.toArray();
	}
	public <E> E[] toArray(E[] a){
		return collection.toArray(a);
	}*/
	
}
