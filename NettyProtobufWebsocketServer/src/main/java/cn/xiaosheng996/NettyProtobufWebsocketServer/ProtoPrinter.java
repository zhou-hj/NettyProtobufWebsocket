package cn.xiaosheng996.NettyProtobufWebsocketServer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.LazyStringList;

public class ProtoPrinter {	
	public static void print(Object object) throws Exception {
		try {
			StringBuilder builder = new StringBuilder();
			
			Class<?> objClass = object.getClass();
			Class<?> builderClass = null;
			for(Class<?> cls : objClass.getDeclaredClasses()) {
				if("Builder".equals(cls.getSimpleName())) {
					builderClass = cls;
					break;
				}
			}
			if (builderClass != null) {
				for(Field field : builderClass.getDeclaredFields()) {
					if(field.getName().startsWith("bitField")) {
						continue;
					}
					if (field.getName().indexOf("Builder") >= 0) {
						continue;
					}
					Method getter = null;
					Object value = null;
					if(field.getType().isAssignableFrom(String.class)) {
						getter = objClass.getDeclaredMethod("get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1).replaceAll("_", ""), new Class[0]);
					} else if(field.getType().isAssignableFrom(List.class)) {
						getter = objClass.getDeclaredMethod("get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1).replaceAll("_", "List"), new Class[0]);
					} else if(field.getType().isAssignableFrom(LazyStringList.class)) {
						getter = objClass.getDeclaredMethod("get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1).replaceAll("_", "List"), new Class[0]);
					} else {
						//System.out.println("get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1).replaceAll("_", ""));
						getter = objClass.getDeclaredMethod("get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1).replaceAll("_", ""), new Class[0]);
					}
					builder.append("\n" + field.getName().replaceAll("_", "") + ":");
					value = getter.invoke(object, new Object[0]);
					//System.out.println(value);
					doPrint(value, "", builder);
				}
			}
			
			System.out.println(builder.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("rawtypes")
	private static void doPrint(Object object, String offset, StringBuilder result) {
		try {
			if(object instanceof String) {
				result.append(object.toString());
			} else if(object instanceof List) {
				result.append("[");
				List list = (List) object;
				for(Object obj : list) {
					doPrint(obj, offset, result);
					result.append(",");
				}
				result.append("]");
			} else if (object instanceof GeneratedMessageV3) {
				Class<?> objClass = object.getClass();
				result.append("{");
				Class<?> builderClass = null;
				for(Class<?> cls : objClass.getDeclaredClasses()) {
					if("Builder".equals(cls.getSimpleName())) {
						builderClass = cls;
						break;
					}
				}
				if (builderClass != null) {
					for(Field field : builderClass.getDeclaredFields()) {
						if("bitField0_".equals(field.getName())) {
							continue;
						}
						Object value = null;
						Method getter = null;
						if(field.getType().isAssignableFrom(String.class)) {
							getter = objClass.getDeclaredMethod("get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1).replaceAll("_", ""), new Class[0]);
						} else if(field.getType().isAssignableFrom(List.class)) {
							getter = objClass.getDeclaredMethod("get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1).replaceAll("_", "List"), new Class[0]);
						} else if(field.getType().isAssignableFrom(LazyStringList.class)) {
							getter = objClass.getDeclaredMethod("get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1).replaceAll("_", "List"), new Class[0]);
						} else {
							getter = objClass.getDeclaredMethod("get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1).replaceAll("_", ""), new Class[0]);
						}
						value = getter.invoke(object, new Object[0]);
						
						result.append("\n" + offset + "\t" + field.getName().replaceAll("_", "") + ":");
						
						doPrint(value, offset + "\t", result);
					}
				}
				result.append("}");
			} else {
				result.append(object.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
