package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileUtil {
	private static final Log logger = LogFactory.getLog(FileUtil.class);

	private static Properties getProperties() {
		Properties p = new Properties();
		try {
			InputStream in = new FileInputStream(new File(
					"img/account.properties"));
			p.load(in);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return null;
		}
		return p;
	}

	public static String getUname() {
		return (String) getProperties().get("uname");
	}

	public static String getMode() {
		return (String) getProperties().get("mode");
	}

	public static String getPwd() {
		return (String) getProperties().get("pwd");
	}

	public static int getThreadCount() {
		int threadCount = Integer.parseInt((String) getProperties().get(
				"threadcount"));
		logger.info("当前设置的线程数量为" + threadCount);
		return threadCount;
	}

	public static String getMsgUserId() {
		String userId = (String) getProperties().get("msguserid");
		return userId;
	}

	public static String getMsgMode() {
		String msgmode = (String) getProperties().get("msgmode");
		return msgmode;
	}

	public static int getFailureCount() {
		return Integer.parseInt((String) getProperties().get("failurecount"));
	}

	public static boolean saveToFile(String string, String filename,
			String contentEncoding) {
		if (string == null)
			return false;
		FileOutputStream fo;
		try {
			fo = new FileOutputStream(new File(filename));
			fo.write(string.getBytes(contentEncoding));
			fo.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}

		return true;
	}

	public static String getStartTime() {
		String startTime = (String) getProperties().get("starttime");
		return startTime;
	}

	public static String getEndTime() {
		String endTime = (String) getProperties().get("endtime");
		return endTime;
	}
}
