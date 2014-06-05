package utils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * 用来加载类，classpath下的资源文件，属性文件等。
 */
public class ClassLoaderUtil {

	/**
	 * 得到类加载器
	 * 
	 * @return
	 */
	private static ClassLoader getClassLoader() {
		return ClassLoaderUtil.class.getClassLoader();
	}

	/**
	 * 得到本Class所在的ClassLoader的Classpat的绝对路径。 URL形式的
	 * 
	 * @return
	 */
	public static String getAbsolutePathOfClassLoaderClassPath() {
		return ClassLoaderUtil.getClassLoader().getResource("").toString();
	}

	/**
	 * @param relativePath
	 *            必须传递资源的相对路径。是相对于classpath的路径。如果需要查找classpath外部的资源，需要使用../来查找
	 * @return 资源的绝对URL
	 * @throwsMalformedURLException
	 */
	private static URL getExtendResource(String relativePath) {
		if (!relativePath.contains("../")) {
			return ClassLoaderUtil.getResource(relativePath);
		}
		String classPathAbsolutePath = ClassLoaderUtil
				.getAbsolutePathOfClassLoaderClassPath();
		if (relativePath.substring(0, 1).equals("/")) {
			relativePath = relativePath.substring(1);
		}
		String wildcardString = relativePath.substring(0,
				relativePath.lastIndexOf("../") + 3);
		relativePath = relativePath
				.substring(relativePath.lastIndexOf("../") + 3);
		int containSum = ClassLoaderUtil.containSum(wildcardString, "../");
		classPathAbsolutePath = ClassLoaderUtil.cutLastString(
				classPathAbsolutePath, "/", containSum);
		String resourceAbsolutePath = classPathAbsolutePath + relativePath;
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
	 * @param resource
	 * @return
	 */
	private static URL getResource(String resource) {
		return ClassLoaderUtil.getClassLoader().getResource(resource);
	}

	/**
	 * 获取应用程序的根目录
	 * 
	 * @return
	 */
	public static String getAppRoot() {
		return getExtendResource("../../../").getPath().substring(1);
	}

	/**
	 * 获取Web应用的根目录
	 * 
	 * @return
	 */
	public static String getWebRoot() {
		return getExtendResource("../../../WebRoot").getPath();
	}

	/**
	 * 获取相对路径的绝对路径，其中相对路径的起始地址为程序APP地址
	 * 
	 * @param relativePath
	 * @return
	 */
	public static String getAppRealPath(String relativePath) {
		String realPath = getAppRoot() + relativePath;
		return realPath.replace("//", "/");
	}

	/**
	 * 获取相对路径的绝对路径，其中相对路径的起始地址为Web程序主目录地址
	 * 
	 * @param relativePath
	 * @return
	 */
	public static String getWebRealPath(String relativePath) {
		String realPath = getAppRoot() + relativePath;
		return realPath.replace("//", "/");
	}

	/**
	 * @paramargs
	 * @throwsMalformedURLException
	 */
	public static void main(String[] args) throws MalformedURLException {
		// System.out.println(ClassLoaderUtil
		// .getExtendResource("../../../img/log4j.properties"));
		System.out.println(getAppRoot());
		System.out.println(getWebRoot());
		System.out.println(getAppRealPath("/WebRoot/"));
	}
}
