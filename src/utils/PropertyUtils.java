package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PropertyUtils {

	private static final Log logger = LogFactory.getLog(PropertyUtils.class);

	/**
	 * 获取一个默认的配置，默认加载img/account.properties
	 * 
	 * @return
	 */
	public static Properties getDefaultProperties() {
		Properties p = new Properties();
		File nf = new File("img/testfile");
		try {
			nf.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		System.out.println(nf.getAbsolutePath());
		try {
			InputStream in = new FileInputStream(new File(
					"img/account.properties"));
			p.load(in);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return p;
	}

	/**
	 * 根据配置文件路径获取一个指定的配置
	 * 
	 * @param path
	 * @return
	 */
	public static Properties getPropertiesByPath(String path) {
		Properties p = new Properties();
		try {
			InputStream in = new FileInputStream(new File(path));
			p.load(in);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return p;
	}

	/**
	 * 获取String配置属性
	 * 
	 * @return
	 */
	public static String getStringProperty(Properties p, String key) {
		return p.getProperty(key);
	}

	/**
	 * 获取int配置属性,若不能解析，将返回0
	 * 
	 * @return
	 */
	public static int getIntProperty(Properties p, String key) {
		int value = 0;
		String str = p.getProperty(key);
		try {
			value = Integer.parseInt(str);
		} catch (NumberFormatException e) {
			logger.error("未能将字符串" + str + "转换成数字！");
			return 0;
		}
		return value;
	}
}
