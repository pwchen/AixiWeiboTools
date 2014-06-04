package utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 用来加载类，classpath下的资源文件，属性文件等。
 * getExtendResource(StringrelativePath)方法，可以使用../符号来加载classpath外部的资源。
 */
public class ClassLoaderUtil {
	private static Log logger = LogFactory.getLog(ClassLoaderUtil.class);

	/** *Thread.currentThread().getContextClassLoader().getResource("") */
	/**
	 * 加载Java类。 使用全限定类名
	 * 
	 * @paramclassName
	 * @return
	 */
	private static Class loadClass(String className) {
		try {
			return getClassLoader().loadClass(className);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("class not found '" + className + "'", e);
		}
	}

	/**
	 * 得到类加载器
	 * 
	 * @return
	 */
	private static ClassLoader getClassLoader() {
		return ClassLoaderUtil.class.getClassLoader();
	}

	/**
	 * 提供相对于classpath的资源路径，返回文件的输入流
	 * 
	 * @param relativePath
	 *            必须传递资源的相对路径。是相对于classpath的路径。如果需要查找classpath外部的资源，需要使用../来查找
	 * @return 文件输入流
	 * @throwsIOException
	 * @throwsMalformedURLException
	 */
	public static InputStream getStream(String relativePath)
			throws MalformedURLException, IOException {
		if (!relativePath.contains("../")) {
			return getClassLoader().getResourceAsStream(relativePath);
		} else {
			return ClassLoaderUtil.getStreamByExtendResource(relativePath);
		}
	}

	/**
	 * @param url
	 * @return
	 * @throwsIOException
	 */
	private static InputStream getStream(URL url) throws IOException {
		if (url != null) {
			return url.openStream();
		} else {
			return null;
		}
	}

	/**
	 * @param relativePath
	 *            必须传递资源的相对路径。是相对于classpath的路径。如果需要查找classpath外部的资源，需要使用../来查找
	 * @return
	 * @throwsMalformedURLException
	 * @throwsIOException
	 */
	public static InputStream getStreamByExtendResource(String relativePath)
			throws MalformedURLException, IOException {
		return ClassLoaderUtil.getStream(ClassLoaderUtil
				.getExtendResource(relativePath));
	}

	/**
	 * 提供相对于classpath的资源路径，返回属性对象，它是一个散列表
	 * 
	 * @param resource
	 * @return
	 */
	private static Properties getProperties(String resource) {
		Properties properties = new Properties();
		try {
			properties.load(getStream(resource));
		} catch (IOException e) {
			throw new RuntimeException("couldn't load properties file '"
					+ resource + "'", e);
		}
		return properties;
	}

	/**
	 * 得到本Class所在的ClassLoader的Classpat的绝对路径。 URL形式的
	 * 
	 * @return
	 */
	public static String getAbsolutePathOfClassLoaderClassPath() {
		ClassLoaderUtil.logger.info(ClassLoaderUtil.getClassLoader()
				.getResource("").toString());
		return ClassLoaderUtil.getClassLoader().getResource("").toString();
	}

	/**
	 * @param relativePath
	 *            必须传递资源的相对路径。是相对于classpath的路径。如果需要查找classpath外部的资源，需要使用../来查找
	 * @return 资源的绝对URL
	 * @throwsMalformedURLException
	 */
	private static URL getExtendResource(String relativePath) {
		ClassLoaderUtil.logger.info("传入的相对路径：" + relativePath);
		if (!relativePath.contains("../")) {
			return ClassLoaderUtil.getResource(relativePath);
		}
		String classPathAbsolutePath = ClassLoaderUtil
				.getAbsolutePathOfClassLoaderClassPath();
		if (relativePath.substring(0, 1).equals("/")) {
			relativePath = relativePath.substring(1);
		}
		ClassLoaderUtil.logger.info(Integer.valueOf(relativePath
				.lastIndexOf("../")));
		String wildcardString = relativePath.substring(0,
				relativePath.lastIndexOf("../") + 3);
		relativePath = relativePath
				.substring(relativePath.lastIndexOf("../") + 3);
		int containSum = ClassLoaderUtil.containSum(wildcardString, "../");
		classPathAbsolutePath = ClassLoaderUtil.cutLastString(
				classPathAbsolutePath, "/", containSum);
		String resourceAbsolutePath = classPathAbsolutePath + relativePath;
		ClassLoaderUtil.logger.info("绝对路径：" + resourceAbsolutePath);
		URL resourceAbsoluteURL = null;
		try {
			resourceAbsoluteURL = new URL(resourceAbsolutePath);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return resourceAbsoluteURL;
	}

	/**
	 * 判断source的前面包含几个dest
	 * 
	 * @param source
	 * @param dest
	 * @return
	 */
	private static int containSum(String source, String dest) {
		int containSum = 0;
		int destLength = dest.length();
		while (source.contains(dest)) {
			containSum = containSum + 1;
			source = source.substring(destLength);
		}
		return containSum;
	}

	/**
	 * 将source的路径向前走num次dest
	 * 
	 * @param source
	 * @param dest
	 * @param num
	 * @return
	 */
	private static String cutLastString(String source, String dest, int num) {
		// String cutSource=null;
		for (int i = 0; i < num; i++) {
			source = source.substring(0,
					source.lastIndexOf(dest, source.length() - 2) + 1);
		}
		return source;
	}

	/**
	 * @paramresource
	 * @return
	 */
	private static URL getResource(String resource) {
		ClassLoaderUtil.logger.info("传入的相对于classpath的路径：" + resource);

		return ClassLoaderUtil.getClassLoader().getResource(resource);
	}

	/**
	 * 获取应用程序的根目录
	 * 
	 * @return
	 */
	public static String getAppRoot() {
		return getExtendResource("../../../").getPath();
	}

	/**
	 * @paramargs
	 * @throwsMalformedURLException
	 */
	public static void main(String[] args) throws MalformedURLException {
		// System.out.println(ClassLoaderUtil
		// .getExtendResource("../../../img/log4j.properties"));
		System.out.println(getAppRoot());
	}

}
