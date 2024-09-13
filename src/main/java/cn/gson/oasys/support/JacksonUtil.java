package cn.gson.oasys.support;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class JacksonUtil {
    static ObjectMapper objectMapper=new ObjectMapper();

    public static String parse(Object obj){
        try {
            return objectMapper.writeValueAsString(obj);
        }catch (JsonProcessingException e){
            System.out.println(e.getLocalizedMessage());
        }
        return "";
    }
    public static JsonNode jsonStringToJsonNode(String s) throws IOException {
        return objectMapper.readTree(s);
    }
    public static<T> T parse(String str,Class<T> clazz){
        try{
            return objectMapper.readValue(str,clazz);
        }catch (IOException e){
            System.out.println(e.getLocalizedMessage());
        }
        return null;
    }

    public static <T,O> T parseWithGenericType(String str, Class<T> clazz,Class<O> type) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            return objectMapper.readValue(str, clazz);
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
        }
        return null;
    }


    public static JsonNode convertToJsonNode(Object obj){
        String jsonString=parse(obj);
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(jsonString);
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
        }
        return jsonNode;
    }
    public static<T> T mapToEntity(Map<String,?> src, Class<T> clazz) throws IllegalAccessException, InstantiationException, IntrospectionException, InvocationTargetException {
        T entity=clazz.newInstance();
        PropertyDescriptor pd;
        for(Field field: clazz.getDeclaredFields()){
            field.setAccessible(true);
            JsonProperty annotation=field.getAnnotation(JsonProperty.class);
            String alias=annotation.value();
            Object value=src.get(alias);
            pd=new PropertyDescriptor(field.getName(),clazz);
            Method method=pd.getWriteMethod();
            method.invoke(entity,value);
        }
        //String n= entity.getClass().getName();
        return entity;
    }

    public static List<ObjectNode> mapToJsonArray(Map<String,String>map){
        List<ObjectNode>nodes = new ArrayList<>();
        for(Map.Entry<String,String> entry:map.entrySet()){
            ObjectNode obj= objectMapper.createObjectNode();
            obj.put("key",entry.getKey());
            obj.put("value",entry.getValue());
            nodes.add(obj);
        }
        return nodes;
    }
    public static ObjectNode mapToNode(Map<String,Object>map){
        return objectMapper.valueToTree(map);
    }

    public static List<ObjectNode>listToJsonArray(List<String> list,boolean needDefault){
        if(list.isEmpty())return Collections.emptyList();
        List<ObjectNode>nodes=new ArrayList<>();
        for(String s:list){
            ObjectNode obj=objectMapper.createObjectNode();
            obj.put("name",s);
            nodes.add(obj);
        }
        if(needDefault){
            ObjectNode objd=objectMapper.createObjectNode();
            objd.put("name","无");
            nodes.add(objd);
        }
        return nodes;
    }

    public static Map<String, Object>  convertEntityToMap(Object entity) throws IllegalAccessException {
        Map<String, Object> map = new HashMap<>();

        // 获取实体类的Class对象
        Class<?> clazz = entity.getClass();

        // 获取所有字段（包括私有字段）
        Field[] fields = clazz.getDeclaredFields();

        // 遍历字段数组
        for (Field field : fields) {
            // 设置可访问私有字段
            field.setAccessible(true);

            // 获取字段名和字段值，并存储到Map中
            String fieldName = field.getName();
            Object fieldValue = field.get(entity);
            map.put(fieldName, fieldValue);
        }
        return map;
    }


}

