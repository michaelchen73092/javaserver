package com.BoBiHealth;

import java.text.SimpleDateFormat;
import java.util.*;
import org.apache.logging.log4j.*;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.LocalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProjectionType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;



public class dynamoDBManager {
	public final DynamoDB dynamoDB = new DynamoDB(new AmazonDynamoDBClient(
	            new ProfileCredentialsProvider("H:\\AWSCredentials.properties","default")));

    private static final Logger logger = LogManager.getLogger(dynamoDBManager.class);
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
	
	public String operationBi(String left,Op op,Object right,Type type,Map<String,String> dictName,Map<String,Object> dictValue){
		if(dictName==null || dictValue==null ){
			logger.error("error!");
			return "";
		}
		String l_name = "#"+left;
		dictName.put(l_name, left);
		String temp_str = ":";
		String ind ="";
		switch (type) {
			case Int:
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
		dictValue.put(temp_str, right);
		if(op==null){
			return_str = l_name+" "+temp_str;
		}else if(op==Op.append || op==Op.not_exit){
			return_str = op.rawValue()+"("+l_name+", "+temp_str+")";
			
		}else{
			return_str = l_name+" "+op.rawValue()+" "+temp_str;
		}
		return return_str;
	}
	public ItemCollection<QueryOutcome> Query(String tabName,Object hashkey,Object sortkey,Op op){
		ItemCollection<QueryOutcome> results = null;


		Table table = dynamoDB.getTable(tabName);
		HashMap<String, String> nameMap = new HashMap<String,String>();
		HashMap<String, Object> valueMap = new HashMap<String,Object>();
		ArrayList<String> keys =  Constants.instance().getKeys(tabName);
		assert(Constants.instance()!=null);
		ArrayList<Type> types = Constants.instance().getTypes(tabName);
		String key1 = operationBi(keys.get(0), Op.eq, hashkey,types.get(0), nameMap, valueMap);
		String key2 = keys.size()<2 ? null:operationBi(keys.get(1), op, sortkey, types.get(1), nameMap, valueMap);
		String finalkey = keys.size()<2 ? key1:(key1+" and "+key2);
		QuerySpec querySpec = new QuerySpec();
		//System.out.println(key1);
		//System.out.println(key2);
		querySpec.withKeyConditionExpression(finalkey);
		querySpec.withNameMap(nameMap);
		querySpec.withValueMap(valueMap);
	
		results = table.query(querySpec);
		assert(results != null);
		//if(results != null) System.out.println("results!=null");
		
		return results;
	}
	


}