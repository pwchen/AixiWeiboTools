package crawler.weibo.service.fetcher;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import utils.CrawlerContext;
import crawler.weibo.model.WeiboLoginedClient;
import crawler.weibo.service.filter.UserFilterService;
import crawler.weibo.service.login.WeiboLoginHttpClientUtils;

/**
 * 采集器，用于获取原始HTML,可以根据原始的HTML内容判断账号是否请求次数过多，或者其他问题
 * 
 * @author Administrator
 * 
 */
public class Fetcher {
	private static final Log logger = LogFactory.getLog(Fetcher.class);

	public static void main(String args[]) {
		WeiboLoginedClient wlClient = WeiboLoginHttpClientUtils
				.getWeiboLoginedClient();
		for (int i = 0; i < 3000; i++) {
			fetchUserInfoHtmlByUid("1936617550", wlClient);
			System.out.println(i);
		}
	}

	/**
	 * 根据用户Id返回改用户的个人信息页面
	 * 
	 * @param userId
	 * @return
	 */
	public static String fetchUserInfoHtmlByUid(String userId,
			WeiboLoginedClient wlClient) {
		String url = "http://weibo.com/" + userId + "/info";
		String entity = fetchRawHtml(url, wlClient);
		if (entity == null) {
			logger.warn(userId + "未知原因");
			UserFilterService.addToFilterList(userId, "未知原因");
			return null;
		} else if (entity.indexOf("账号异常") != -1) {
			logger.warn(userId + "账号异常");
			UserFilterService.addToFilterList(userId, "账号异常");
			return null;
		} else if (entity.indexOf("页面不存在") != -1
				|| entity.indexOf("页面地址有误") != -1) {
			logger.warn(url + "页面地址有误，或者该页面不存在");
			UserFilterService.addToFilterList(userId, "页面地址有误，或者该页面不存在");
			return null;
		}
		return entity;
	}

	/**
	 * 根据用户ID以及页面号，返回改用户的粉丝列表页面
	 * 
	 * @param userId
	 * @param page
	 * @return
	 */
	public static String fetchUserFans(String userId, int page,
			WeiboLoginedClient wlClient) {
		String url = "http://weibo.com/" + userId + "/fans?page=" + page;
		String entity = Fetcher.fetchRawHtml(url, wlClient);
		if (entity.indexOf("页面不存在") != -1) {
			logger.info("页面不存在" + url);
			return null;
		} else if (entity.indexOf("账号异常") != -1) {
			logger.info("账号异常:" + url);
			return Fetcher.fetchRawHtml(url, wlClient);
		}
		return entity;
	}

	/**
	 * 根据用户ID以及页面号，返回改用户的关注列表页面
	 * 
	 * @param userId
	 * @param page
	 * @return
	 */
	public static String fetchUserFollows(String userId, int page,
			WeiboLoginedClient wlClient) {
		String url = "http://weibo.com/" + userId + "/follow?page=" + page;
		String entity = Fetcher.fetchRawHtml(url, wlClient);
		if (entity.indexOf("页面不存在") != -1) {
			logger.info("页面不存在" + url);
			return null;
		} else if (entity.indexOf("账号异常") != -1) {
			logger.info("账号异常:" + url);
			return Fetcher.fetchRawHtml(url, wlClient);
		}
		return entity;
	}

	/**
	 * 获取制定url的网页内容
	 * 
	 * @param client
	 * @param personalUrl
	 * @return
	 */
	public static String fetchRawHtml(String url, WeiboLoginedClient wlClient) {
		wlClient = checkWeiboLoginClient(wlClient);
		HttpGet getMethod = new HttpGet(url);
		String entityStr = null;
		InputStream in = null;
		int count = 0;
		boolean exception = true;// 出异常
		int failureCount = CrawlerContext.getContext().getFailureNumber();
		while (count++ < CrawlerContext.getContext().getFailureNumber()
				&& exception) {
			HttpClient client = wlClient.getClient();
			exception = false;
			/**** 获取页面html start ****/
			try {
				HttpResponse response = client.execute(getMethod);
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					in = entity.getContent(); // 之前没使用这个造成了大量异常抛出
				}
				entityStr = EntityUtils.toString(entity, "utf-8");
			} catch (IOException e) {
				exception = true;
				logger.warn(e);
				continue;
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			/**** 获取页面html end ****/

			if (entityStr.indexOf("抱歉，网络繁忙") != -1) {
				logger.error("抱歉，网络繁忙:" + url + "等待3秒后换号...");
				wlClient = changeAnotherAccount();
				exception = true;
				continue;
			} else if (entityStr.indexOf("正确输入验证码答案") != -1) {
				logger.error("正确输入验证码答案:" + url + "等待3秒后换号...");
				wlClient = changeAnotherAccount();
				exception = true;
				continue;
			} else if (entityStr.indexOf("违反了新浪微博的安全检测规则") != -1) {
				logger.error("违反了新浪微博的安全检测规则:" + url + "等待3秒后换号...");
				wlClient = changeAnotherAccount();
				exception = true;
				continue;
			} else if (entityStr.indexOf("抱歉，您当前访问的帐号异常，暂时无法访问。") != -1) {// 目标账号问题，不需要换号
				logger.error("抱歉，您当前访问的帐号异常，暂时无法访问:" + url);
				return "账号异常";
			} else if (entityStr.indexOf("还没有微博帐号？现在加入") != -1
					|| entityStr.indexOf("赶快注册微博粉我吧") != -1) {
				logger.error("还没有微博帐号？现在加入" + url + "等待3秒后换号...");
				wlClient = changeAnotherAccount();
				exception = true;
				return "注册微博";
			} else if (entityStr
					.indexOf("The server returned an invalid or incomplete response") != -1) {
				logger.error("502 Bad Gateway,The server returned an invalid or incomplete response:"
						+ url + "等待3秒后换号...");
				wlClient = changeAnotherAccount();
				exception = true;
			} else if (entityStr.indexOf("页面地址有误") != -1) {
				return "页面不存在";
			} else if (entityStr.indexOf("location.replace(") != -1) {
				int locationIndex = entityStr.indexOf("location.replace(");
				String location = entityStr.substring(locationIndex + 18);
				location = location.substring(0, location.indexOf("\");"));
				// location = location.substring(0,
				// location.indexOf("ticket="));
				exception = true;
				logger.info(url + "地址跳转：" + location);
				continue;
			}
		}
		if (count > failureCount) {
			logger.error("超时次数超过最大次数:" + failureCount + "放弃此次请求:" + url);
			return null;
		} else if (entityStr == null) {
			logger.error("未知原因:" + url);
		}
		return entityStr;
	}

	/**
	 * 检查客户端是否超过请求次数限制
	 * 
	 * @param wlClient
	 * @return
	 */
	private static WeiboLoginedClient checkWeiboLoginClient(
			WeiboLoginedClient wlClient) {
		int requestNumber = CrawlerContext.getContext().getRequestNumber();
		int randomNumber = (int) (Math.random() * requestNumber * 0.15);// 向下浮动50%
		int reqCount = wlClient.getReqCount() + 1;
		if (reqCount >= requestNumber - randomNumber) {// 请求次数超过最大请求，换号
			logger.warn("当前请求次数reqCount:" + reqCount + ".请求次数超过最大请求"
					+ requestNumber + "，换号");
			WeiboLoginedClient newClient = changeAnotherAccount();
			newClient.setReqCount(1);
			return newClient;
		}
		wlClient.setReqCount(reqCount);
		return wlClient;
	}

	/**
	 * 换号
	 * 
	 * @return
	 */
	private static WeiboLoginedClient changeAnotherAccount() {
		try {
			Thread.currentThread().sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return WeiboLoginHttpClientUtils.getWeiboLoginedClient();
	}
}
