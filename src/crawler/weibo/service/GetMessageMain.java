package crawler.weibo.service;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;

import utils.FileUtil;
import crawler.weibo.dao.UserJdbcService;
import crawler.weibo.model.WeiboUser;

public class GetMessageMain {
	private HttpClient httpClient;
	private static final Log logger = LogFactory.getLog(GetMessageMain.class);
	private String userId = FileUtil.getMsgUserId();
	private String startTime = FileUtil.getStartTime();
	private String endTime = FileUtil.getEndTime();
	private WeiboUser weiboUser;
	private Queue<String> fQueue = new ArrayDeque<String>();

	public void startGetMessage(HttpClient httpClient) {
		this.httpClient = httpClient;
		logger.info(Thread.currentThread().getName() + "启动爬取微博模式...");
		System.out.println(Thread.currentThread().getName() + "启动爬取微博模式...");
		// String[] userIds = FileUtil.getMsgUserId().split(",");
		List<String> userIds = UserJdbcService.getInstance()
				.getUserIDfromUser();
		logger.info("入口用户有" + userIds.size() + ",分别是" + userIds);
		System.out.println("入口用户有" + userIds.size() + ",分别是" + userIds);
		for (int j = 0; j < userIds.size(); j++) {
			String tempUserId = userIds.get(j);
			logger.warn(tempUserId);
			System.out.println(Thread.currentThread().getName() + "当前入口用户为"
					+ tempUserId);
			int threadCount = Thread.getAllStackTraces().size();
			int maxThreadCount = FileUtil.getThreadCount();

			userId = tempUserId;
			weiboUser = UserJdbcService.getInstance().getWeiboUser(userId);
			if (weiboUser == null) { // 爬取用户信息，并保存入库
				weiboUser = new GetUserInfoThread(httpClient)
						.getWeiboUser(userId);
			}

			String followUserIds = weiboUser.getFollowUserId();
			String[] followList = followUserIds.split(",");
			fQueue.add(userId);
			for (String followStr : followList) {
				fQueue.add(followStr);
			}
			for (int i = 0; i < maxThreadCount; i++) {
				GetMessageThread gmt = new GetMessageThread(this);
				Thread tempThread = new Thread(gmt);
				tempThread.start();
			}
			while (Thread.getAllStackTraces().size() > threadCount) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public HttpClient getHttpClient() {
		return httpClient;
	}

	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public synchronized String getFollow() {
		return fQueue.poll();
	}

	public synchronized int getQueSize() {
		return fQueue.size();
	}

	public WeiboUser getWeiboUser() {
		return weiboUser;
	}

	public void setWeiboUser(WeiboUser weiboUser) {
		this.weiboUser = weiboUser;
	}

}
