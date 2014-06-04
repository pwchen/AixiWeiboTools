package utils;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import utils.PropertyUtils;

/**
 * 微博采集爬虫上下文，配置文件
 * @author Administrator
 *
 */
public class CrawlerContext {
	/**
	 * 新浪账户用户名列表
	 */
	private String[] userNameList;
	/**
	 * 新浪账户用户名密码列表，对应于新浪用户名列表
	 */
	private String[] userPwdList;
	/**
	 * 新浪账户数量
	 */
	private int userNumber;
	/**
	 * 新浪账户公共密码
	 */
	private String commonPwd;
	/**
	 * 线程数量
	 */
	private int threadNumber;

	/**
	 * 连接失败的次数
	 */
	private int failureNumber;

	/**
	 * 爬取入口ID列表
	 */
	private String[] initUserIdList;

	/**
	 * 每次登陆最大请求数量
	 */
	private int requestNumber;

	private static CrawlerContext crawlerContext = null;

	private static final Log logger = LogFactory.getLog(CrawlerContext.class);

	public String[] getUserNameList() {
		return userNameList;
	}

	public void setUserNameList(String[] userNameList) {
		this.userNameList = userNameList;
	}

	public String[] getUserPwdList() {
		return userPwdList;
	}

	public void setUserPwdList(String[] userPwdList) {
		this.userPwdList = userPwdList;
	}

	public int getUserNumber() {
		return userNumber;
	}

	public void setUserNumber(int userNumber) {
		this.userNumber = userNumber;
	}

	public String getCommonPwd() {
		return commonPwd;
	}

	public void setCommonPwd(String commonPwd) {
		this.commonPwd = commonPwd;
	}

	public int getThreadNumber() {
		return threadNumber;
	}

	public void setThreadNumber(int threadNumber) {
		this.threadNumber = threadNumber;
	}

	public int getFailureNumber() {
		return failureNumber;
	}

	public void setFailureNumber(int failureNumber) {
		this.failureNumber = failureNumber;
	}

	public String[] getInitUserIdList() {
		return initUserIdList;
	}

	public void setInitUserIdList(String[] initUserIdList) {
		this.initUserIdList = initUserIdList;
	}

	public int getRequestNumber() {
		return requestNumber;
	}

	public void setRequestNumber(int requestNumber) {
		this.requestNumber = requestNumber;
	}

	// 单例
	public CrawlerContext() {
	}

	/**
	 * 单例懒汉方法，返回爬虫配置上下文
	 * 
	 * @return CrawlerContext
	 */
	public synchronized static CrawlerContext getContext() {
		if (crawlerContext == null) {
			crawlerContext = new CrawlerContext();
			crawlerContext.initCrawlerContext();
		}
		return crawlerContext;
	}

	/**
	 * 加载配置文件
	 */
	private void initCrawlerContext() {
		Properties p = PropertyUtils.getDefaultProperties();
		this.userNameList = PropertyUtils.getStringProperty(p, "unamelist")
				.split(",");
		this.userNumber = userNameList.length;
		logger.info("在配置文件中读取了"+userNumber+"个微博账户");
		this.userPwdList = PropertyUtils.getStringProperty(p, "pwdlist").split(
				",");
		this.commonPwd = PropertyUtils.getStringProperty(p, "commonpwd");
		this.threadNumber = PropertyUtils.getIntProperty(p, "threadnumber");
		logger.info("当前线程数量为："+threadNumber);
		this.failureNumber = PropertyUtils.getIntProperty(p, "failurenumber");
		logger.info("允许连接失败最大次数为："+failureNumber);
		this.initUserIdList = PropertyUtils.getStringProperty(p,
				"inituseridlist").split(",");
		this.requestNumber =PropertyUtils.getIntProperty(p, "requestnumber");
		logger.info("当前每次登陆最大的请求数量为："+requestNumber);
	}
}
