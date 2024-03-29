package com.BoBiHealth.dynamoDB;

import java.text.SimpleDateFormat;
import java.math.*;
import java.util.*;
import org.apache.logging.log4j.*;
import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.*;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.model.*;
import java.util.*;
import java.util.concurrent.*;
import com.BoBiHealth.Servlet.TimeZoneID;
import bolts.*;;




public class dynamoDBManager {
	private AmazonDynamoDBAsync dynamoDB ;
	private static dynamoDBManager inst = null;
    private static final Logger logger = LogManager.getLogger(dynamoDBManager.class);
	public static dynamoDBManager instance() {
		if(inst==null){
			inst = new dynamoDBManager();
		}
		return inst;
	}
	public  dynamoDBManager(){
	boolean flag = false;
		while(!flag){
			try{
				this.dynamoDB = new AmazonDynamoDBAsyncClient(
		            new ProfileCredentialsProvider("H:\\AWSCredentials.properties","default"));
				flag = true;
			}catch(Exception exception){
				System.out.println(exception.getMessage());
			}
		}
	}
    public String conCat(String delit,ArrayList<String> strs){
		Integer count =0;
		String return_str = "";
		Iterator<String> ite = strs.iterator();
		while(ite.hasNext()){
			if(count==0){
				return_str += ite.next();
			}else{
				return_str += (delit+ite.next());
			}
		}
		return return_str;
	}
	public String updateOpExp(String op, String exp)
	{	
		String result_str = op+" "+exp;
		return result_str;
	}
	public static void main(String args[]){
		AttributeValue attributeValue = new AttributeValue();
		attributeValue.setN("345");
		if(attributeValue.getS() != null){
			System.out.printf("can access S: %s\n",attributeValue.getS());
		}else if(attributeValue.getBOOL() != null){
			System.out.printf("convert to Boolean %s\n",attributeValue.toString());
		}else if(attributeValue.getL() != null){
			System.out.printf("convert to List %s\n",attributeValue.toString());
		}else if(attributeValue.getM() != null){
			System.out.printf("convert to Map %s\n",attributeValue.toString());
		}else if(attributeValue.getNS() != null){
			System.out.printf("convert to Number Set %s\n",attributeValue.toString());

		}else if(attributeValue.getSS() != null){
			System.out.printf("convert to String Set %s\n",attributeValue.toString());

		}else if(attributeValue.getN() != null){
			System.out.printf("convert to Number %s\n",attributeValue.toString());

		}
		TimeZoneID timeZoneID = TimeZoneID.instance;
		for(BigDecimal offset:timeZoneID.id_map){
			List<PutItemRequest> list = new java.util.ArrayList<>();
			for(int i=1;i<32;i++){
				PutItemRequest request = new PutItemRequest();
				Map<String, Object> key_map = new HashMap<>();
				key_map.put("timezone_ID", offset.toString());
				key_map.put("day", new BigDecimal(i));
				key_map.put("month", new BigDecimal(9));
				key_map.put("year", new BigDecimal(2016));
				key_map.put("time", new HashMap<>());
				request.withTableName("Appointment");
				request.withItem((new ItemV2(key_map)).toAttributeValueMap());
				list.add(request);
			}
			/*try{
				dynamoDBManager.instance().BatchputItemAsync(list, 100);
			}catch(InterruptedException e){
				return;
			}*/
		}

	}
	public void createTableAsync(String tabName,String hashkey,String hashtype,String sortkey,String sorttype){
		CreateTableRequest request = new CreateTableRequest();
		HashSet<AttributeDefinition> attr_set = new HashSet<>();
		attr_set.add((new AttributeDefinition()).withAttributeName(hashkey).withAttributeType(hashtype));
		if(sortkey != null)attr_set.add((new AttributeDefinition()).withAttributeName(sortkey).withAttributeType(sorttype));
		ArrayList<KeySchemaElement> keydef_set = new ArrayList<>();
		keydef_set.add((new KeySchemaElement()).withAttributeName(hashkey).withKeyType(KeyType.HASH));
		if(sortkey != null) keydef_set.add((new KeySchemaElement()).withAttributeName(sorttype).withKeyType(KeyType.RANGE));
		request.setAttributeDefinitions(attr_set);
		request.setKeySchema(keydef_set);
		request.setTableName(tabName);
		request.withProvisionedThroughput((new ProvisionedThroughput()).withReadCapacityUnits(new Long(5)).withWriteCapacityUnits(new Long(5)));
		Task<Object> task = dynamoDBManager.instance().createTableAsync(request);
		try{
			task.waitForCompletion();
		}catch(InterruptedException exception){
			return;
		}
		if(task.isFaulted()){
			Exception exception = task.getError();
			System.out.println(exception.getMessage());
		}else{
			System.out.println("create table!!");
		}
	}
	public Task<Void> BatchputItemAsync(Collection<PutItemRequest> collection,long delay) throws InterruptedException{
		List<Task<Object>> list = new ArrayList<>();
		for(PutItemRequest item:collection){
			list.add(putItemAsync(item));
			if(delay>0){
				Thread.sleep(delay);
			}
		}
		return Task.whenAll(list);
	}
	public Task<Object> putItemAsync(PutItemRequest request){
		TaskCompletionSource<Object> taskCompletionSource = new TaskCompletionSource<>();
		dynamoDB.putItemAsync(request, new AsyncHandler<PutItemRequest,PutItemResult>(){
			public void onSuccess(PutItemRequest puItemRequest,PutItemResult putItemResult){
				System.out.println("task success!!");
				taskCompletionSource.setResult(null);			
			}
			public void onError(Exception exception){
				System.out.println("task doomed!!");
				System.out.println(exception.getMessage());
				taskCompletionSource.setError(exception);
			}
		});
		return taskCompletionSource.getTask();
	}
	public String operationBi(String left,Op op,Object right,Type type,Map<String,String> dictName,Map<String,AttributeValue> dictValue){
		if(dictName==null || dictValue==null ){
			logger.error("error!");
			return "";
		}
		String l_name = "#"+left;
		dictName.put(l_name, left);
		String temp_str = ":";
		String ind ="";
		switch (type) {
			case Number:
				ind = "I";
				break;
			case Double:
				ind = "D";
				break;
			case Str:
				ind = "S";
				break;
			case Str_Set:
				ind = "Ss";
				break;
			case Bool:
				ind = "B";
				break;
			case List:
				ind = "L";
				break;
			default:
				break;
		}
		temp_str += ind;
		String return_str = "";
		while(dictValue.keySet().contains(temp_str)){
			temp_str += ind;
		}
		AttributeValue attributeValue = new AttributeValue();
		switch (type) {
		case Number:
			attributeValue.setN(((BigDecimal)right).toString());
			break;
		case Double:
			attributeValue.setN(((Integer)right).toString());
			break;
		case Str:
			attributeValue.setS(((String)right));
			break;
		case Str_Set:
			attributeValue.setSS(((Collection<String>)right));
			break;
		case Bool:
			attributeValue.setBOOL(((Boolean)right));
			break;
		case List:
			attributeValue.setL(((Collection<AttributeValue>)right));
			break;
		default:
			break;
	}
		dictValue.put(temp_str, attributeValue);
		
		if(op==null){
			return_str = l_name+" "+temp_str;
		}else if(op==Op.append || op==Op.not_exit){
			return_str = op.rawValue()+"("+l_name+", "+temp_str+")";
			
		}else{
			return_str = l_name+" "+op.rawValue()+" "+temp_str;
		}
		return return_str;
	}
	
	public Task<Object> openAppoint(String tabName,ItemV2 item){
		ItemV2 key = (ItemV2) item.get("keys");
		ItemV2 value = (ItemV2) item.get("value");
		TaskCompletionSource<Object> taskCompletionSource = new TaskCompletionSource<>();
		UpdateItemRequest request = new UpdateItemRequest();
		request.withAttributeUpdates(value.toAttributeValueUpdate(AttributeAction.PUT));
		request.withKey(key.toAttributeValueMap());
		request.withTableName(tabName);
		dynamoDB.updateItemAsync(request, new AsyncHandler<UpdateItemRequest,UpdateItemResult>(){
			public void onSuccess(UpdateItemRequest updateItemRequest,UpdateItemResult updateItemResult){
				System.out.println("task success!!");
				taskCompletionSource.setResult(null);			
			}
			public void onError(Exception exception){
				System.out.println("task doomed!!");

				taskCompletionSource.setError(exception);
			}
		});
		return taskCompletionSource.getTask();
	}
	public Task<Object> updateItemAsync(UpdateItemRequest request){
		TaskCompletionSource<Object> taskCompletionSource = new TaskCompletionSource<>();
		dynamoDB.updateItemAsync(request, new AsyncHandler<UpdateItemRequest,UpdateItemResult>(){
			public void onSuccess(UpdateItemRequest updateItemRequest,UpdateItemResult updateItemResult){
				System.out.println("task success!!");
				taskCompletionSource.setResult(null);			
			}
			public void onError(Exception exception){
				System.out.println("task doomed!!");

				taskCompletionSource.setError(exception);
			}
		});
		return taskCompletionSource.getTask();
	}
	public Task<Object> createTableAsync(CreateTableRequest request){
		TaskCompletionSource<Object> taskCompletionSource = new TaskCompletionSource<>();
		dynamoDB.createTableAsync(request, new AsyncHandler<CreateTableRequest,CreateTableResult>(){
			public void onSuccess(CreateTableRequest createTableRequest,CreateTableResult createTableResult){
				System.out.println("task success!!");
				taskCompletionSource.setResult(null);			
			}
			public void onError(Exception exception){
				System.out.println("task doomed!!");

				taskCompletionSource.setError(exception);
			}
		});
		return taskCompletionSource.getTask();
	}
	public Task<Object> deleteItemAsync(DeleteItemRequest request){
		TaskCompletionSource<Object> taskCompletionSource = new TaskCompletionSource<>();
		dynamoDB.deleteItemAsync(request, new AsyncHandler<DeleteItemRequest,DeleteItemResult>(){
			public void onSuccess(DeleteItemRequest deleteItemRequest,DeleteItemResult deleteItemResult){
				System.out.println("task success!!");
				taskCompletionSource.setResult(null);			
			}
			public void onError(Exception exception){
				System.out.println("task doomed!!");

				taskCompletionSource.setError(exception);
			}
		});
		return taskCompletionSource.getTask();
	}
	public Task<CollectionWrapper<ItemV2>> Query(String tabName,Object hashkey,Object sortkey,Op op){
		 //Task<Collection<ItemV2>> results = null;
		 final TaskCompletionSource<CollectionWrapper<ItemV2>> taskCompletionSource = new TaskCompletionSource<CollectionWrapper<ItemV2>>();

		HashMap<String, String> nameMap = new HashMap<String,String>();
		HashMap<String, AttributeValue> valueMap = new HashMap<String,AttributeValue>();
		ArrayList<String> keys =  Constants.instance().getKeys(tabName);
		assert(Constants.instance()!=null);
		ArrayList<Type> types = Constants.instance().getTypes(tabName);
		String key1 = operationBi(keys.get(0), Op.eq, hashkey,types.get(0), nameMap, valueMap);
		String key2 = keys.size()<2 ? null:operationBi(keys.get(1), op, sortkey, types.get(1), nameMap, valueMap);
		String finalkey = keys.size()<2 ? key1:(key1+" and "+key2);
		QueryRequest queryre = new QueryRequest();
		//System.out.println(key1);
		//System.out.println(key2);
		queryre.withTableName(tabName);
		queryre.withKeyConditionExpression(finalkey);
		queryre.setExpressionAttributeNames(nameMap);
		queryre.setExpressionAttributeValues(valueMap);
		System.out.println("before initiate the client");
		dynamoDB.queryAsync(queryre,new AsyncHandler<QueryRequest,QueryResult>(){
			public void onSuccess(QueryRequest queryRequest,QueryResult queryResult){
				List<Map<String,AttributeValue>> result = queryResult.getItems();
				CollectionWrapper<ItemV2> collection = new CollectionWrapper<ItemV2>(queryRequest,queryResult.getLastEvaluatedKey());
				System.out.println("task success!!");
				taskCompletionSource.setResult(tranForm(collection,result));
				
			}
			public void onError(Exception exception){
				System.out.println("task doomed!!");

				taskCompletionSource.setError(exception);
			}
		});
		//assert(results != null);
		//if(results != null) System.out.println("results!=null");
		return taskCompletionSource.getTask();
	}
	private CollectionWrapper<ItemV2> tranForm(CollectionWrapper<ItemV2> return_result,List<Map<String, AttributeValue>> result){
		Iterator<Map<String, AttributeValue>> iterator = result.iterator();
		while(iterator.hasNext()){
			Map<String, AttributeValue> map = iterator.next();
			return_result.add(new ItemV2(map));
		}
		return return_result;
	}
	private Collection<ItemV2> tranForm(Collection<ItemV2> return_result,List<Map<String, AttributeValue>> result){
		Iterator<Map<String, AttributeValue>> iterator = result.iterator();
		while(iterator.hasNext()){
			Map<String, AttributeValue> map = iterator.next();
			return_result.add(new ItemV2(map));
		}
		return return_result;
	}
	Task<Object>queryAsyncNexPage(QueryRequest query,final Collection<ItemV2> collection,final Map<String, AttributeValue> laskKey){
		final TaskCompletionSource<Object> taskCompletionSource = new TaskCompletionSource<Object>();
		query.setExclusiveStartKey(new HashMap<String, AttributeValue>(laskKey));
		laskKey.clear();
		collection.clear();
		dynamoDB.queryAsync(query,new AsyncHandler<QueryRequest,QueryResult>(){
			public void onSuccess(QueryRequest queryRequest,QueryResult queryResult){
				List<Map<String,AttributeValue>> result = queryResult.getItems();
				Map<String, AttributeValue> buffer_laskKey = queryResult.getLastEvaluatedKey();
				if(buffer_laskKey != null){
					laskKey.putAll(buffer_laskKey);
				}
				System.out.println("task success!!");
				taskCompletionSource.setResult(tranForm(collection, result));
				
			}
			public void onError(Exception exception){
				System.out.println("task doomed!!");
				taskCompletionSource.setError(exception);
			}
		});
		//assert(results !
		return taskCompletionSource.getTask();
	}
	public Task<CollectionWrapper<ItemV2>> Scan(String tabName){
		final TaskCompletionSource<CollectionWrapper<ItemV2>> taskCompletionSource = new TaskCompletionSource<CollectionWrapper<ItemV2>>();
		assert(Constants.instance()!=null);
		ScanRequest scanRequest = new ScanRequest();
		scanRequest.withTableName(tabName);
		dynamoDB.scanAsync(scanRequest,new AsyncHandler<ScanRequest, ScanResult>(){
			public void onSuccess(ScanRequest scanRequest,ScanResult scanResult){
				List<Map<String, AttributeValue>> result = scanResult.getItems();
				CollectionWrapper<ItemV2> collection = new CollectionWrapper<ItemV2>(scanRequest, scanResult.getLastEvaluatedKey());
				if(scanResult.getLastEvaluatedKey() != null){
					System.out.println("scan is not finished");
				}else{
					System.out.println("scan is finished");
				}
				taskCompletionSource.setResult(tranForm(collection, result));
			}
			public void onError(Exception exception){
				taskCompletionSource.setError(exception);
			}
		});
		//System.out.println(key1);
		//System.out.println(key2);
		//assert(results != null);
		
		return taskCompletionSource.getTask();
	}
	Task<Object>scanAsyncNexPage(ScanRequest scan,final Collection<ItemV2> collection,final Map<String, AttributeValue> laskKey){
		final TaskCompletionSource<Object> taskCompletionSource = new TaskCompletionSource<Object>();
		scan.setExclusiveStartKey(new HashMap<String, AttributeValue>(laskKey));
		laskKey.clear();
		collection.clear();
		dynamoDB.scanAsync(scan,new AsyncHandler<ScanRequest,ScanResult>(){
			public void onSuccess(ScanRequest scanRequest,ScanResult scanResult){
				List<Map<String,AttributeValue>> result = scanResult.getItems();
				Map<String, AttributeValue> buffer_laskKey = scanResult.getLastEvaluatedKey();
				if(buffer_laskKey != null){
					laskKey.putAll(buffer_laskKey);
				}
				taskCompletionSource.setResult(tranForm(collection, result));
				
			}
			public void onError(Exception exception){
				taskCompletionSource.setError(exception);
			}
		});
		//assert(results !
		return taskCompletionSource.getTask();
	}


}