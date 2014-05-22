package crawler.weibo.test;

import java.lang.reflect.Method;
import java.sql.Timestamp;

public class tes1 {

	public static void main(String[] args) {
		Timestamp t1 = Timestamp.valueOf("2010-07-29 00:00:00");
		Timestamp t2 = Timestamp.valueOf("2010-07-28 00:00:00");
		System.out.println(t1.after(t2));
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