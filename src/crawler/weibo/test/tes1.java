package crawler.weibo.test;

import java.lang.reflect.Method;
import java.sql.Timestamp;

public class tes1 {

	public static void main(String[] args) {
		String[] userArr1 = null;
		for (String str : userArr1)
			System.out.println(str);
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