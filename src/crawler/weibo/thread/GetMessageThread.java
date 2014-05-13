package crawler.weibo.thread;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;

import utils.FeedUtils;
import utils.MsgJdbcService;
import utils.UserJdbcService;
import crawler.weibo.model.WeiboUser;

public class GetMessageThread implements Runnable {
	private HttpClient httpClient;
	private GetMessageMain getMessageMain;
	private String userId;
	private WeiboUser weiboUser;
	private String startTime;
	private String endTime;
	private static final Log logger = LogFactory.getLog(GetMessageThread.class);

	public GetMessageThread(GetMessageMain getMessageMain) {
		this.getMessageMain = getMessageMain;
		this.weiboUser = getMessageMain.getWeiboUser();
		this.startTime = getMessageMain.getStartTime();
		this.endTime = getMessageMain.getEndTime();
		this.httpClient = getMessageMain.getHttpClient();
	}

	public void run() {
		while ((this.userId = getMessageMain.getFollow()) != null) {
			logger.info(Thread.currentThread().getName() + "开始采集" + userId
					+ "--" + startTime + "微博消息");
			System.out.println(Thread.currentThread().getName() + "开始采集"
					+ weiboUser.getScreenName() + "--" + startTime
					+ "的微博消息，剩余用户数" + getMessageMain.getQueSize());
			if (!userId.equals(weiboUser.getUserId())) {// 如果不是第一入口用户ID
				weiboUser = UserJdbcService.getInstance().getWeiboUser(userId);
				if (weiboUser == null) {
					// 数据库中没有此用户信息，爬取用户信息，并入库
					weiboUser = new GetUserInfoThread(
							getMessageMain.getHttpClient())
							.getWeiboUser(userId);
				}
			}

			if (weiboUser == null) {
				continue;
			}
			if (weiboUser.getMsgExistent() == null
					|| "0".equals(weiboUser.getMsgExistent())) {

				if (MsgJdbcService.getInstance().getMsgCountById(
						weiboUser.getUserId()) >= (weiboUser.getFansNum() / 10)) {
					logger.info(Thread.currentThread().getName() + ">>--"
							+ userId + "用户的微博消息已采集");
					System.out
							.println(Thread.currentThread().getName() + ">>--"
									+ weiboUser.getScreenName() + "用户的微博消息已采集");
				} else {
					FeedUtils feedUtils = new FeedUtils();
					int msgCount = feedUtils.getPersonalPageWeibo(httpClient,
							weiboUser, startTime, endTime);
					logger.info(Thread.currentThread().getName() + "采集"
							+ userId + "用户的微博消息结束，已采集到" + msgCount + "条微博");
					System.out.println(Thread.currentThread().getName() + "采集"
							+ weiboUser.getScreenName() + "用户的微博消息结束，已采集到"
							+ msgCount + "条微博");
				}
				weiboUser.setMsgExistent("1");
				UserJdbcService.getInstance().upDateWeiboUser(weiboUser);
			} else {
				logger.info(Thread.currentThread().getName() + "--" + userId
						+ "用户的微博消息已采集");
				System.out.println(Thread.currentThread().getName() + "--"
						+ weiboUser.getScreenName() + "用户的微博消息已采集");
			}
		}
		logger.info(Thread.currentThread().getName() + "线程退出...");
		System.out.println(Thread.currentThread().getName() + "线程退出...");
		endTime = "";
	}
}
