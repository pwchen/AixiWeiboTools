package crawler.weibo.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class tes1 {

	public static void main(String[] args) {
		t1 tt1 = new t1();
		Class cl = tt1.getClass();
		try {
			cl.getMethod(findMethodName(cl, "name1")).invoke(tt1);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static String findMethodName(Class userClass, String columnName) {
		String getMethodStr = "get" + columnName;
		Method[] methods = userClass.getMethods();
		for (int i = 0; i < methods.length; i++) {
			String methodStr = methods[i].getName();
			if (methodStr.equalsIgnoreCase(getMethodStr)) {
				return methodStr;
			}
		}
		return null;
	}
}

class t1 {
	public static String a1 = "11";

	public void getName() {
		System.out.println(11111111);
	}
}