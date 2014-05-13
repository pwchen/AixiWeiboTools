package crawler.weibo.test;

import org.apache.http.client.HttpClient;

import utils.FeedUtils;
import utils.FileUtil;
import utils.UserJdbcService;
import crawler.weibo.login.WeiboLogin;
import crawler.weibo.model.WeiboUser;
import crawler.weibo.service.GetUserInfoThread;

public class Test {

	public static void main(String[] args) {
		String userIdStr = "1197161814";// 1197161814,1182389073,1182391231,1662429125,1293974281,1648237865,1807303633,1784501333,1894091913,1820201245,1666234801,1788911247,1683472301";
		String[] userIds = userIdStr.split(",");
		HttpClient client = WeiboLogin.getLoginStatus();
		int failureCount = 0;
		while (client == null) {
			if (failureCount++ > FileUtil.getFailureCount()) {
				System.out.println(Thread.currentThread().getName()
						+ "连接失败次数已经超过最大连接数！程序关闭......");
				try {
					Thread.sleep(20000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.exit(1);
			}
			System.out.println(Thread.currentThread().getName()
					+ "连接网络失败！20秒后重新连接......");
			try {
				Thread.sleep(20000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			client = WeiboLogin.getLoginStatus();
		}
		for (String userId : userIds) {
			WeiboUser weiboUser = UserJdbcService.getInstance().getWeiboUser(
					userId);
			if (weiboUser == null) {
				// 数据库中没有此用户信息，爬取用户信息，并入库
				weiboUser = new GetUserInfoThread(client).getWeiboUser(userId);
			}
			FeedUtils feedUtils = new FeedUtils();
			int msgCount = feedUtils.getPersonalPageWeibo(client, weiboUser,
					"2011-01-01", null);
			System.out.println(msgCount);
		}
	}

}
