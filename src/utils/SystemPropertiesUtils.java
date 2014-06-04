package utils;

import java.util.Properties;

public class SystemPropertiesUtils {
	/**
	 * 按需添加方法
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Java的运行环境版本："
				+ PROPERTIES.getProperty("java.version"));
		System.out.println("Java的运行环境供应商："
				+ PROPERTIES.getProperty("java.vendor"));
		System.out.println("Java供应商的URL："
				+ PROPERTIES.getProperty("java.vendor.url"));
		System.out.println("Java的安装路径：" + PROPERTIES.getProperty("java.home"));
		System.out.println("Java的虚拟机规范版本："
				+ PROPERTIES.getProperty("java.vm.specification.version"));
		System.out.println("Java的虚拟机规范供应商："
				+ PROPERTIES.getProperty("java.vm.specification.vendor"));
		System.out.println("Java的虚拟机规范名称："
				+ PROPERTIES.getProperty("java.vm.specification.name"));
		System.out.println("Java的虚拟机实现版本："
				+ PROPERTIES.getProperty("java.vm.version"));
		System.out.println("Java的虚拟机实现供应商："
				+ PROPERTIES.getProperty("java.vm.vendor"));
		System.out.println("Java的虚拟机实现名称："
				+ PROPERTIES.getProperty("java.vm.name"));
		System.out.println("Java运行时环境规范版本："
				+ PROPERTIES.getProperty("java.specification.version"));
		System.out.println("Java运行时环境规范供应商："
				+ PROPERTIES.getProperty("java.specification.vender"));
		System.out.println("Java运行时环境规范名称："
				+ PROPERTIES.getProperty("java.specification.name"));
		System.out.println("Java的类格式版本号："
				+ PROPERTIES.getProperty("java.class.version"));
		System.out.println("Java的类路径："
				+ PROPERTIES.getProperty("java.class.path"));
		System.out.println("加载库时搜索的路径列表："
				+ PROPERTIES.getProperty("java.library.path"));
		System.out.println("默认的临时文件路径："
				+ PROPERTIES.getProperty("java.io.tmpdir"));
		System.out.println("一个或多个扩展目录的路径："
				+ PROPERTIES.getProperty("java.ext.dirs"));
		System.out.println("操作系统的名称：" + PROPERTIES.getProperty("os.name"));
		System.out.println("操作系统的构架：" + PROPERTIES.getProperty("os.arch"));
		System.out.println("操作系统的版本：" + PROPERTIES.getProperty("os.version"));
		System.out.println("文件分隔符：" + PROPERTIES.getProperty("file.separator")); // 在unix系统中是＂／＂
		System.out.println("路径分隔符：" + PROPERTIES.getProperty("path.separator")); // 在unix系统中是＂:＂
		System.out.println("行分隔符：" + PROPERTIES.getProperty("line.separator")); // 在unix系统中是＂/n＂
		System.out.println("用户的账户名称：" + PROPERTIES.getProperty("user.name"));
		System.out.println("用户的主目录：" + PROPERTIES.getProperty("user.home"));
		System.out.println("用户的当前工作目录：" + PROPERTIES.getProperty("user.dir"));
	}

	/* system properties to get separators */
	static final Properties PROPERTIES = new Properties(System.getProperties());

	/**
	 * get line separator on current platform
	 * 
	 * @return line separator
	 */
	public static String getLineSeparator() {
		return PROPERTIES.getProperty("line.separator");
	}

	/**
	 * get path separator on current platform
	 * 
	 * @return path separator
	 */
	public static String getPathSeparator() {
		return PROPERTIES.getProperty("path.separator");
	}

	/**
	 * get file separator on current platform
	 * 
	 * @return path separator
	 */
	public static String getFileSeparator() {
		return PROPERTIES.getProperty("file.separator");
	}

	/**
	 * get user dir on current platform
	 * 
	 * @return path separator
	 */
	public static String getUserDir() {
		return PROPERTIES.getProperty("user.dir");
	}

}
