package com.BoBiHealth.dynamoDB;
import com.amazonaws.services.dynamodbv2.model.*;
import java.util.*;
import java.math.*;
import org.json.*;
public class ItemV2 extends JSONObject{
	//private JSONObject item;
	public ItemV2(Map<String, ? extends Object> item){
		Iterator<String> iterator =  item.keySet().iterator();
		//this.item = new JSONObject();
		while(iterator.hasNext()){
			String index = iterator.next();
			Object attributeValue = item.get(index);
			if(attributeValue instanceof AttributeValue){
				super.put(index, parse((AttributeValue)attributeValue));
			}else{
				super.put(index, transForm(attributeValue));
			}
		}
		
	}
	public ItemV2(String str){
		super(str);
		System.out.printf("string is %s\n",str);
		//System.out.printf("this string:%s\n",toString());
		//System.out.printf("super string:%s\n", super.toString());
	}
	public JSONObject getJSONObject(){
		return this;
	}
	public String String(){
		Iterator<String> keys = keys();
		while(keys.hasNext()){
			String key = keys.next();
			System.out.printf("key:%s\n",key);
		}
		return toString();
	}
	public static void main(String args[]){
		List<Object> test_list = new ArrayList<>();
		test_list.add("test1");
		test_list.add(new BigDecimal("34.33"));
		JSONArray test_jsonarray = new JSONArray();
		test_jsonarray.put(new ItemV2("{name:chienlin}"));
		System.out.printf("testJson's format:%s\n", test_jsonarray.toString());
		System.out.printf("list(2)'s class name:%s\n",test_jsonarray.get(0).getClass().getName());
		Map<String, AttributeValue> map = new HashMap<String, AttributeValue>();
		map.put("name1", new AttributeValue().withS("Chien-Lin1"));
		map.put("name2", new AttributeValue().withS("Chien-Lin2"));
		Map<String, AttributeValue> map2 = new HashMap<String, AttributeValue>(map);
		map.put("map", new AttributeValue().withM(map2));
		ItemV2 itemV2 = new ItemV2(map);
		itemV2.put("number", new Float(12.333));
		ItemV2 map3 = (ItemV2) itemV2.get("map");
		System.out.printf("map: %s\n",(String) map3.get("name1"));
		System.out.printf("map: %s\n",(String) map3.get("name2"));
		System.out.printf("%s\n", itemV2.get("name1"));
		System.out.printf("text format: %s\n",itemV2.toString());
		ItemV2 test_item = new ItemV2("{name1:[Number,3442.22],name2:[Map,{inner1:[String,chienlin],inner2:[Number,344]}]}");
		JSONObject jsonObject = new JSONObject("{name1:3442.22}");
		ItemV2 inner = (ItemV2) test_item.get("name2");
		System.out.printf("text format: %s\n",test_item.toString());
		System.out.printf("json text format: %s\n",jsonObject.toString());
		//System.out.printf("name1 again:%s\n", (String) test_item.get("name1"));
		System.out.println(inner.get("delta"));
		System.out.printf("inner text format: %s\n",inner.toString());

		//System.out.printf("class name:%s\n", itemV2.get(number));

	}
	
	@Override
	public JSONObject put(String key, double value){
		BigDecimal numb = new BigDecimal(value);
		super.put(key, numb);
		return this;
	}
	
	@Override
	public JSONObject put(String key, Object value){
		String classname = value.getClass().getName();
		System.out.printf("!!!!classname is %s\n",classname);
		if(value instanceof JSONArray){
			JSONArray value1 = (JSONArray) value;
			selfParse(value1);
			
		}
		super.put(key, value);
		
		return this;
	}
	private void selfParse(JSONArray value1){
		if(value1.length()>0){
			Object type = value1.get(0);
			if(type instanceof String){
				String real_type = (String) type;
				JSONArray real_value;
				int length;
				Object trial;
				switch(real_type){
				case "NumberSet": 
					real_value = (JSONArray) value1.get(1);
					length = real_value.length();
					JSONArray temp_buffer = new JSONArray();
					for(int i=0;i<length;i++){
						trial = real_value.get(i);
						if(trial instanceof BigDecimal) continue;
						temp_buffer.put( new BigDecimal(trial.toString()));
					}
					value1.put(1,temp_buffer);

					break;
				case "Number":
						trial = value1.get(1);
						if(trial instanceof BigDecimal) break;
						BigDecimal num = new BigDecimal(trial.toString());
						value1.put(1,num);
					break;
				case "Map":
					JSONObject jsonObject = value1.getJSONObject(1);
					ItemV2 new_item = new ItemV2(jsonObject.toString());
					value1.put(1,new_item);
					break;
				case "List":
					real_value = (JSONArray) value1.get(1);
					length = real_value.length();
					for(int i=0;i<length;i++){
						JSONArray tem_arry = real_value.getJSONArray(i);
						selfParse(tem_arry);
					}
					break;
				default: 
				}
			}
		}
	}
	
	public void store(String key, Object value){
		super.put(key, transForm(value));
		return;


	}
	
	
	private JSONArray transForm(Object value){
		JSONArray jsonArray = new JSONArray();

		if(value instanceof String){
			String string = (String) value;
			System.out.printf("can access S: %s\n",string);
			jsonArray.put("String");
			jsonArray.put(string);
			return jsonArray;
		}else if(value instanceof Boolean){
			Boolean boolean1 = (Boolean) value;
			System.out.printf("convert to Boolean %s\n",boolean1.toString());
			jsonArray.put("Boolean");
			jsonArray.put(boolean1);
			return jsonArray;
		}else if(value instanceof Collection<?>){
			if(value instanceof List<?>){
				List<Object> list = (List<Object>) value;
				System.out.printf("convert to List:%s\n",list.toString());
				jsonArray.put("List");
				List<JSONArray> new_list = new ArrayList<JSONArray>();
				Iterator<Object> iterator = (list).iterator();
				while(iterator.hasNext()){
					Object attr = iterator.next();
					new_list.add(transForm(attr));
				}
				jsonArray.put(new_list);
				return jsonArray;
			}else{
				Collection<Object> collection = (Collection<Object>) value;
				Iterator<Object> iterator = collection.iterator();
				Object buffer=null;
				while(iterator.hasNext()){
					buffer = iterator.next();
				}
				if(buffer instanceof String){
					jsonArray.put("StringSet");
					jsonArray.put(collection);

				}else if(buffer instanceof BigDecimal){
					jsonArray.put("NumberSet");
					jsonArray.put(collection);
				}
				return jsonArray;
			}
		}else if(value instanceof Map<?, ?>){
			Map<String, Object> map = (Map<String, Object>) value;
			System.out.printf("convert to Map %s\n",map.toString());
			ItemV2 new_map = new ItemV2(map);
			jsonArray.put("Map");
			jsonArray.put(new_map);
			System.out.printf("jsonArray's string:%s\n",jsonArray.toString());

			return jsonArray;
		}else if(value instanceof BigDecimal){
			System.out.printf("convert to Number %s\n",value.toString());
			jsonArray.put("Number");
			jsonArray.put(value);
			return jsonArray;
		}else{
			System.out.println("unresolved typde");
			return null;
		}
	}
	
	public Map<String, AttributeValueUpdate> toAttributeValueUpdate(AttributeAction action){
		Iterator<String> keys = keys();
		Map<String, AttributeValueUpdate> map = new HashMap<String, AttributeValueUpdate>();
		while(keys.hasNext()){
			AttributeValueUpdate result = new AttributeValueUpdate();
			result.setAction(action);
			String key = keys.next();
			result.setValue(toAttributeValue((JSONArray)super.get(key)));
			map.put(key, result);
		}
		return map;
	}
	public Map<String, AttributeValue> toAttributeValueMap(){
		Iterator<String> keys = keys();
		Map<String, AttributeValue> map = new HashMap<String, AttributeValue>();
		while(keys.hasNext()){
			String key = keys.next();
			Object retrun_value = super.get(key);
			System.out.printf("%s:%s\n",key,retrun_value.getClass().getName());
			map.put(key, toAttributeValue((JSONArray)retrun_value));
		}
		return map;
	}
	public Object get(String attrName){
		JSONArray jsonArray;
		Object buff;
		try{
			buff = super.get(attrName);
		}catch(Exception exception){
			return null;
		}
		if(buff instanceof JSONArray){
			jsonArray = (JSONArray) buff;
			Object buff2 = jsonArray.get(0);
			if(buff2 instanceof String){
				String type = (String) buff2;
				int length;
				JSONArray value;
				switch(type){
					case "Number": return jsonArray.get(1);
					case "String": return jsonArray.get(1);
					case "Boolean": return jsonArray.get(1);
					case "List": 
								value = (JSONArray)jsonArray.get(1);
								length = value.length();
								List<JSONArray> list = new ArrayList<>();
								for(int i=0;i<length;i++){
									list.add(value.getJSONArray(i));
								}
								return list;
					case "Map": return jsonArray.get(1);
					case "NumberSet": value = (JSONArray)jsonArray.get(1);
									  length = value.length();
									  Collection<BigDecimal> set = new HashSet<>();
									  for(int i=0;i<length;i++){
										  set.add((BigDecimal)value.get(i));
									  }
									  return set;
					case "StringSet": value = (JSONArray)jsonArray.get(1);
									  length = value.length();
									  Collection<String> set_str = new HashSet<>();
									  for(int i=0;i<length;i++){
										  set_str.add(value.getString(i));
									  }
									  return set_str;
					default: return jsonArray;
				}
			}else{
				return jsonArray;
			}
		}else{
			return buff;
		}
		
	}
	private String getType(String attrName){
		JSONArray jsonArray = (JSONArray) super.get(attrName);
		return jsonArray.getString(0);
	}
	private AttributeValue toAttributeValue(JSONArray jsonArray){
		AttributeValue attributeValue = new AttributeValue();
		int length ;
		String type = jsonArray.getString(0);
		JSONArray buffer;
		switch(type){
		case "String":attributeValue.setS((String)jsonArray.getString(1));
						break;
		case "Boolean":attributeValue.setBOOL((Boolean)jsonArray.getBoolean(1));
						break;
		case "Number":attributeValue.setN(((BigDecimal)jsonArray.get(1)).toString());
						break;
		case "List": 	buffer =(JSONArray) jsonArray.get(1);
						length = buffer.length();
						List<AttributeValue> list = new ArrayList<>();
						for(int i=0;i<length;i++){
							JSONArray jsonArray_inner = buffer.getJSONArray(i);
							list.add(toAttributeValue(jsonArray_inner));
						}
						attributeValue.setL(list);
						break;
		case "Map":		attributeValue.setM(((ItemV2)jsonArray.get(1)).toAttributeValueMap());	
						break;
		case "NumberSet": buffer =(JSONArray) jsonArray.get(1);
						  length = buffer.length();
						  Collection<String> list2 = new HashSet<>();
						  for(int i=0;i<length;i++){
							  BigDecimal temp_num =(BigDecimal) buffer.get(i);
							  list2.add(temp_num.toString());
						  }
						  attributeValue.setNS(list2);
						  break;
		case "StringSet": buffer =(JSONArray) jsonArray.get(1);
						  length = buffer.length();
						  Collection<String> list3 = new HashSet<>();
						  for(int i=0;i<length;i++){
							  String str =(String) buffer.get(i);
							  list3.add(str);
						  }
						  attributeValue.setSS(list3);
						  break;
		default: System.out.println("unknown type!!!");
						
		}
		return attributeValue;
	}
	private JSONArray parse(AttributeValue attributeValue){
		if(attributeValue == null) return null;
		String string = attributeValue.getS();
		Boolean boolean1 = attributeValue.getBOOL();
		List<AttributeValue> list = attributeValue.getL();
		Map<String, AttributeValue> map = attributeValue.getM();
		List<String> list2 = attributeValue.getNS();
		List<String> list3 = attributeValue.getSS();
		String num = attributeValue.getN();
		JSONArray jsonArray = new JSONArray();

		if(string != null){
			System.out.printf("can access S: %s\n",string);
			jsonArray.put("String");
			jsonArray.put(string);
			return jsonArray;
		}else if(boolean1 != null){
			System.out.printf("convert to Boolean %s\n",attributeValue.toString());
			jsonArray.put("Boolean");
			jsonArray.put(boolean1);
			return jsonArray;
		}else if(list != null){
			System.out.printf("convert to List %s\n",attributeValue.toString());
			jsonArray.put("List");
			List<JSONArray> new_list = new ArrayList<JSONArray>();
			Iterator<AttributeValue> iterator = list.iterator();
			while(iterator.hasNext()){
				AttributeValue attr = iterator.next();
				new_list.add(parse(attr));
			}
			jsonArray.put(new_list);
			return jsonArray;
		}else if(map != null){
			System.out.printf("convert to Map %s\n",attributeValue.toString());
			ItemV2 new_map = new ItemV2(map);
			jsonArray.put("Map");
			jsonArray.put(new_map);
			System.out.printf("jsonArray's string:%s\n",jsonArray.toString());

			return jsonArray;
		}else if(list2 != null){
			System.out.printf("convert to Number Set %s\n",attributeValue.toString());
			Set<BigDecimal> new_set = new HashSet<BigDecimal>();
			jsonArray.put("NumberSet");
			Iterator<String> iterator = list2.iterator();
			while(iterator.hasNext()){
				new_set.add(new BigDecimal(iterator.next()));
			}
			jsonArray.put(new_set);

			return jsonArray;
		}else if(list3 != null){
			System.out.printf("convert to String Set %s\n",attributeValue.toString());
			Set<String> new_set = new HashSet<String>(list3);
			jsonArray.put("StringSet");
			jsonArray.put(new_set);
			return jsonArray;

		}else if(num != null){
			System.out.printf("convert to Number %s\n",attributeValue.toString());
			jsonArray.put("Number");
			jsonArray.put(new BigDecimal(num));
			return jsonArray;
		}else{
			System.out.println("unresolved typde");
			return null;
		}
	}

}
