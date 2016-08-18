package com.BoBiHealth;
import com.amazonaws.services.dynamodbv2.model.*;
import java.util.*;
import java.math.*;
import org.json.*;
public class ItemV2 {
	private JSONObject item;
	public ItemV2(Map<String, AttributeValue> item){
		Iterator<String> iterator =  item.keySet().iterator();
		this.item = new JSONObject();
		while(iterator.hasNext()){
			String index = iterator.next();
			AttributeValue attributeValue = item.get(index);
			this.item.put(index, parse(attributeValue));
		}
		
	}
	public JSONObject getJSONObject(){
		return item;
	}
	public static void main(String args[]){
		Map<String, AttributeValue> map = new HashMap<String, AttributeValue>();
		map.put("name1", new AttributeValue().withS("Chien-Lin1"));
		map.put("name2", new AttributeValue().withS("Chien-Lin2"));
		Map<String, AttributeValue> map2 = new HashMap<String, AttributeValue>(map);
		map.put("map", new AttributeValue().withM(map2));
		ItemV2 itemV2 = new ItemV2(map);
		Map<String, Object> map3 = (Map<String, Object>) itemV2.get("map");
		System.out.printf("map: %s\n",(String) map3.get("name1"));
		System.out.printf("map: %s\n",(String) map3.get("name2"));
		System.out.printf("%s", itemV2.get("name1"));


	}
	public Object get(String attrName){
		return this.item.get(attrName);
	}
	private Object parse(AttributeValue attributeValue){
		if(attributeValue == null) return null;
		String string = attributeValue.getS();
		Boolean boolean1 = attributeValue.getBOOL();
		List<AttributeValue> list = attributeValue.getL();
		Map<String, AttributeValue> map = attributeValue.getM();
		List<String> list2 = attributeValue.getNS();
		List<String> list3 = attributeValue.getSS();
		String num = attributeValue.getN();
		if(string != null){
			System.out.printf("can access S: %s\n",string);
			return string;
		}else if(boolean1 != null){
			System.out.printf("convert to Boolean %s\n",attributeValue.toString());
			return boolean1;
			
		}else if(list != null){
			System.out.printf("convert to List %s\n",attributeValue.toString());
			List<Object> new_list = new ArrayList<Object>();
			Iterator<AttributeValue> iterator = list.iterator();
			while(iterator.hasNext()){
				AttributeValue attr = iterator.next();
				new_list.add(parse(attr));
			}
			return new_list;
		}else if(map != null){
			System.out.printf("convert to Map %s\n",attributeValue.toString());
			JSONObject new_map = new JSONObject();
			Iterator<String> iterator = map.keySet().iterator();
			while (iterator.hasNext()) {
				String index = iterator.next();
				AttributeValue attr = map.get(index);
				new_map.put(index, parse(attr));
				
			}
			return new_map;
		}else if(list2 != null){
			System.out.printf("convert to Number Set %s\n",attributeValue.toString());
			Set<BigDecimal> new_set = new HashSet<BigDecimal>();
			Iterator<String> iterator = list2.iterator();
			while(iterator.hasNext()){
				new_set.add(new BigDecimal(iterator.next()));
			}
			return new_set;
		}else if(list3 != null){
			System.out.printf("convert to String Set %s\n",attributeValue.toString());
			Set<String> new_set = new HashSet<String>(list3);
			return new_set;

		}else if(num != null){
			System.out.printf("convert to Number %s\n",attributeValue.toString());
			return new BigDecimal(num);
		}else{
			System.out.println("unresolved typde");
			return null;
		}
	}
}
