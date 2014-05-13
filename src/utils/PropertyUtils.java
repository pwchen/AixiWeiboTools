package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyUtils {

	private static Properties p = null;

	private static Properties getProperties() {
		if (p == null) {
			synchronized (PropertyUtils.class) {
				if (p == null) {
					p = new Properties();
					try {
						InputStream in = new FileInputStream(new File(
								"img/account.properties"));
						p.load(in);
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
						return null;
					}
				}
			}
		}
		return p;
	}

	/**
	 * 获取微博登录用户名
	 * 
	 * @return
	 */
	public static String getUname() {
		return getProperties().getProperty("uname");
	}

	/**
	 * 获取微博登录密码
	 * 
	 * @return
	 */
	public static String getPwd() {
		return getProperties().getProperty("pwd");
	}

	public static int getFailureCount() {
		return Integer.parseInt(getProperties().getProperty("failurecount"));
	}

	public static int getMaxThreadCount() {
		return Integer.parseInt(getProperties().getProperty("threadcount"));
	}

	public static int getReqcount() {
		return Integer.parseInt(getProperties().getProperty("reqcount"));
	}

}
