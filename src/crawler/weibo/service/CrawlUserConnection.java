package crawler.weibo.service;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.edu.gdut.utils.PropertyUtils;
import cn.edu.gdut.utils.UserJdbcService;

public class CrawlUserConnection {
	private static int MaxThreadSize = PropertyUtils.getMaxThreadCount();
	private Long[] userIdArr;
	private int currentIndex = 740;
	private int count;// 已获取的评论数量
	private static final Log logger = LogFactory
			.getLog(CrawlUserConnection.class);
	private static ArrayList<String> userIdList = UserJdbcService.getInstance()
			.getUserIdList();

	CrawlUserConnection(Long[] userIdArr, int count) {
		this.userIdArr = userIdArr;
		this.count = count;
	}

	/**
	 * 获取当前的采集进度
	 * 
	 * @return
	 */
	String getStatusReport() {
		return currentIndex + "/" + count;
	}

	/**
	 * 查询ID是否在userIdList中
	 * 
	 * @param userId
	 * @return
	 */
	synchronized static boolean checkUserIdInUserIdList(String userId) {
		if ((userIdList.contains(userId))) {
			return true;
		} else {
			addUserIdToUserIdList(userId);
			return false;
		}
	}

	/**
	 * 将用户Id加入userIdList
	 * 
	 * @param userId
	 * @return
	 */
	static boolean addUserIdToUserIdList(String userId) {
		return userIdList.add(userId);
	}

	/**
	 * 从转发ID列表中获取一个ID，用于爬取该ID的用户及关注信息
	 * 
	 * @return
	 */
	synchronized long getUserIdfromIdArr() {
		if (currentIndex < count) {
			logger.info("取第" + (currentIndex + 1) + "个用户ID，剩余："
					+ (count - currentIndex - 1));
			return userIdArr[currentIndex++];
		}
		logger.info("全部" + (count) + "个用户ID，已经获取完毕.");
		return 0;
	}

	/**
	 * 获取所用用户信息，及所用用户的关注用户信息
	 */
	void getUserConnection() {
		CrawlUserAndFollowsThread cuft = new CrawlUserAndFollowsThread(this);
		for (int i = 0; i < MaxThreadSize; i++) {
			new Thread(cuft).start();
		}
	}
}
