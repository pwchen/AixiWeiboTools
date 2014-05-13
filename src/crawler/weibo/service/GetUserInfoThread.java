package crawler.weibo.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;

import utils.FeedUtils;
import utils.FileUtil;
import utils.UserJdbcService;
import crawler.weibo.model.WeiboUser;

public class GetUserInfoThread implements Runnable {
	private final HttpClient httpClient;
	private static final Log logger = LogFactory
			.getLog(GetUserInfoThread.class);

	public GetUserInfoThread(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	private String getUser() {
		return UserJdbcService.getInstance().getUserIDfromQueue();
	}

	private int insertUser(WeiboUser weiboUser) {
		return UserJdbcService.getInstance().inSertWeiboUser(weiboUser);
	}

	private int updateUser(WeiboUser weiboUser) {
		return UserJdbcService.getInstance().upDateWeiboUser(weiboUser);
	}

	private WeiboUser getUser(String uid) {
		return UserJdbcService.getInstance().getWeiboUser(uid);
	}

	public void run() {
		System.out.println(Thread.currentThread().getName()
				+ Thread.currentThread().getName() + "启动...");
		while (true) {
			String userId = getUser();
			if (userId == null) {// 等待其他线程将userId插入数据库队列
				System.out.println(Thread.currentThread().getName()
						+ Thread.currentThread().getName()
						+ "没有可用的用户ID了,休息1s...");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				getWeiboUser(userId);
			}
		}
	}

	public WeiboUser getWeiboUser(String userId) {
		int failure = 0;
		// if (userId.equals(FileUtil.getUid())) {
		// continue;// 跳过自己
		// }
		FeedUtils utils = new FeedUtils();
		WeiboUser weiboUser = null;
		weiboUser = utils.getWeiboUserInfo(httpClient, userId);
		if (weiboUser == null) {// 为获取到次ID数据，跳入下一个ID
			logger.error("获取id为" + userId + "用户基本信息失败!");
			System.out.println(Thread.currentThread().getName() + "获取id为"
					+ userId + "用户基本信息失败!");
			if (failure++ > FileUtil.getFailureCount()) {
				logger.error("连续获取失败了" + FileUtil.getFailureCount() + "次，线程退出!");
				System.out.println(Thread.currentThread().getName() + "连续获取失败了"
						+ FileUtil.getFailureCount() + "次，线程退出!");
				Thread.currentThread().stop();
			}
			return null;
		}
		WeiboUser oWeiboUser = getUser(userId);
		if (oWeiboUser == null) {
			weiboUser = utils.getWeiboAllUserInfo(httpClient, weiboUser);
			if (insertUser(weiboUser) == 1) {
				logger.info(weiboUser.getScreenName() + ":新增入库成功!");
				System.out.println(Thread.currentThread().getName()
						+ weiboUser.getScreenName() + ":新增入库成功!");
			} else {
				logger.error(weiboUser.getScreenName() + ":新增入库失败!");
				System.out.println(Thread.currentThread().getName()
						+ weiboUser.getScreenName() + ":新增入库失败!");
			}
		} else {
			int oldSum = oWeiboUser.getMessageNum() + oWeiboUser.getFollowNum();
			int newSum = weiboUser.getMessageNum() + weiboUser.getFollowNum();
			if (newSum > oldSum) {
				logger.info(weiboUser.getScreenName() + "：原关注数-"
						+ oWeiboUser.getFollowNum() + ",原微博数-"
						+ oWeiboUser.getMessageNum() + ",现关注数-"
						+ weiboUser.getFollowNum() + ",现微博数-"
						+ weiboUser.getMessageNum() + ",变动-"
						+ (newSum - oldSum));
				System.out.println(Thread.currentThread().getName()
						+ weiboUser.getScreenName() + "：原关注数-"
						+ oWeiboUser.getFollowNum() + ",原微博数-"
						+ oWeiboUser.getMessageNum() + ",现关注数-"
						+ weiboUser.getFollowNum() + ",现微博数-"
						+ weiboUser.getMessageNum() + ",变动-"
						+ (newSum - oldSum));
				weiboUser = utils.getWeiboAllUserInfo(httpClient, weiboUser);
				if (updateUser(weiboUser) == 1) {
					logger.info(weiboUser.getScreenName() + ":更新入库成功!");
					System.out.println(Thread.currentThread().getName()
							+ weiboUser.getScreenName() + ":更新入库成功!");
				} else {
					logger.error(weiboUser.getScreenName() + ":更新入库失败!");
					System.out.println(Thread.currentThread().getName()
							+ weiboUser.getScreenName() + ":更新入库失败!");
				}
			} else {
				System.out.println(Thread.currentThread().getName()
						+ weiboUser.getScreenName() + "不需要更新！");
				UserJdbcService.getInstance().updateUserIdfomQueue(userId);
			}
			// 这里以后可以继续加入微博信息爬取方法
		}
		return weiboUser;
	}
}
