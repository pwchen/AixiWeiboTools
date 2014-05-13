package utils;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.select.Elements;

import crawler.weibo.model.WeiboMsg;
import crawler.weibo.model.WeiboUser;

public class FeedUtils {
	private static final Log logger = LogFactory.getLog(FeedUtils.class);
	private static ParseFeed feed = new ParseFeed();
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * 通过API调用获取指定uid下的页的微博
	 * 
	 * @param client
	 * @param apiUrl
	 * @param uid
	 * @return
	 * @throws ParseException
	 */
	private List<WeiboMsg> parseWBMsgFromApiHtmlData(HttpClient client,
			String url, String uid, WeiboUser weiboUser) {
		String weiboFeedHtmlData = null;
		Elements elements;
		List<WeiboMsg> apiMsgs = null;
		// 通过api的方式获取到首页微博信息
		try {
			int count = 0;
			while (weiboFeedHtmlData == null
					&& count <= FileUtil.getFailureCount()) {
				if (++count > 1) {
					logger.info("正在第" + count + "次获取" + url + "...");
					System.out.println(Thread.currentThread().getName() + "正在第"
							+ count + "次获取" + url + "...");
				}
				weiboFeedHtmlData = getRawHtml(client, url);
			}
			if (weiboFeedHtmlData == null) {// 5次还未获取，则放弃
				return null;
			}
			// 将得到的数据去除多余的字符格式化为json
			weiboFeedHtmlData = weiboFeedHtmlData.replace("\\/", "/");
			JSONObject jo = new JSONObject(weiboFeedHtmlData);

			// 获得微博html数据
			weiboFeedHtmlData = jo.getString("data");
			// FileUtil.saveToFile(weiboFeedHtmlData, "weiboFeedHtmlData.html",
			// "utf-8");
			elements = feed.getWeiboContentElements(weiboFeedHtmlData);
			apiMsgs = feed.parsePersonalWeibo(elements, weiboUser);
		} catch (JSONException e) {
			logger.error(e);
		}
		return apiMsgs;
	}

	/**
	 * 获取指定ID的微博列表数量，并存储
	 * 
	 * @param client
	 * @param personalUrl
	 * @param uid
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws JSONException
	 * @throws ParseException
	 */
	public int getPersonalPageWeibo(HttpClient client, WeiboUser weiboUser,
			String startTime, String endTime) {
		// 第一次Get页面html，获取页数
		String url = "http://weibo.com/" + weiboUser.getUserId();
		String entity = null;
		int count = 0;
		while (entity == null && count <= FileUtil.getFailureCount()) {
			if (++count > 1) {
				logger.info("正在第" + count + "次获取" + url + "...");
				System.out.println(Thread.currentThread().getName() + "正在第"
						+ count + "次获取" + url + "...");
			}
			entity = getRawHtml(client, url);
		}
		if (entity == null) {// 5次还未获取，则放弃
			logger.error(Thread.currentThread().getName()
					+ weiboUser.getScreenName() + "5次获取失败！放弃获取该用户！");
			System.out.println(Thread.currentThread().getName()
					+ weiboUser.getScreenName() + "5次获取失败！放弃获取该用户！");
			return 0;
		}
		// FileUtil.saveToFile(entity, "entity.html", "utf-8");
		String weiboFeedHtmlData = feed.getHisNormalHTMLData(entity);

		// 获取搜索的微博数量
		int msgCount = feed.parseFeedCount(entity);
		// 获取微博content elements
		Elements elements = feed.getWeiboContentElements(weiboFeedHtmlData);
		List<WeiboMsg> msgs = feed.parsePersonalWeibo(elements, weiboUser);
		logger.info(Thread.currentThread().getName() + "当前用户userId："
				+ weiboUser.getUserId() + " 获取首页1/3微博数" + msgs.size());
		System.out.println(Thread.currentThread().getName() + "当前用户："
				+ weiboUser.getScreenName() + " 获取首页1/3微博数" + msgs.size());

		if (msgs.size() == 0)
			return 0;

		try {// 说明已经找到了所有在最晚时间范围内的微博了
			if (msgs.get(msgs.size() - 1).getPublicTime()
					.before(sdf.parse(startTime))) {
				return msgs.size();
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		if (msgCount <= 15) {// 如果搜索的微博数量小于等于15，则直接返回
			return msgCount;
		}

		int pageCount = (msgCount % 45 == 0) ? msgCount / 45
				: msgCount / 45 + 1;
		logger.info(" userId：" + weiboUser.getUserId() + " weiboCount: "
				+ msgCount + " pageCount:" + pageCount);
		System.out.println(Thread.currentThread().getName() + " 当前用户："
				+ weiboUser.getScreenName() + " 微博总数: " + msgCount
				+ " 需爬取的总页数:" + pageCount);
		String maxId = msgs.get(msgs.size() - 1).getId();
		if (msgCount > 15) {
			// 第一次lazyload
			String homeRequest = "http://weibo.com/aj/mblog/mbloglist?_wv=5&page="
					+ 1
					+ "&count=15&max_id="
					+ maxId
					+ "&pre_page="
					+ 1
					+ "&end_id=&pagebar=0&uid=" + weiboUser.getUserId();
			List<WeiboMsg> apiMsgs = parseWBMsgFromApiHtmlData(client,
					homeRequest, weiboUser.getUserId(), weiboUser);
			if (apiMsgs.size() == 0) {
				logger.info(Thread.currentThread().getName()
						+ "获取微博失败,已经获取微博总数" + msgs.size());
				System.out.println(Thread.currentThread().getName()
						+ "获取微博失败,已经获取微博总数" + msgs.size());
				return apiMsgs.size();
			}
			msgs.addAll(apiMsgs);
			logger.info("当前用户userId：" + weiboUser.getUserId() + "获取首页2/3微博数"
					+ apiMsgs.size() + ",已经获取微博总数" + msgs.size());
			System.out.println(Thread.currentThread().getName() + "当前用户："
					+ weiboUser.getScreenName() + " 获取首页2/3微博数"
					+ apiMsgs.size() + ",已经获取微博总数" + msgs.size());

			try {// 说明已经找到了所有在最晚时间范围内的微博了
				if (msgs.get(msgs.size() - 1).getPublicTime()
						.before(sdf.parse(startTime))) {
					return msgs.size();
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}

			if (msgCount > 30) {
				// 第二次lazyload
				homeRequest = "http://weibo.com/aj/mblog/mbloglist?_wv=5&page="
						+ 1 + "&count=15&max_id=" + maxId + "&pre_page=" + 1
						+ "&end_id=&pagebar=1&uid=" + weiboUser.getUserId();
				apiMsgs = parseWBMsgFromApiHtmlData(client, homeRequest,
						weiboUser.getUserId(), weiboUser);
				msgs.addAll(apiMsgs);
				logger.info("当前用户userId：" + weiboUser.getUserId()
						+ "获取首页3/3微博数" + apiMsgs.size() + ",已经获取微博总数"
						+ msgs.size());
				System.out.println(Thread.currentThread().getName() + "当前用户："
						+ weiboUser.getScreenName() + " 获取首页3/3微博数"
						+ apiMsgs.size() + ",已经获取微博总数" + msgs.size());
				try {// 说明已经找到了所有在最晚时间范围内的微博了
					if (msgs.get(msgs.size() - 1).getPublicTime()
							.before(sdf.parse(startTime))) {
						return msgs.size();
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}

		for (int i = 2; pageCount > 1 && i <= pageCount; i++) {
			String homeRequest = "http://weibo.com/aj/mblog/mbloglist?_wv=5&page="
					+ i
					+ "&count=15&max_id=&pre_page="
					+ (i - 1)
					+ "&end_id=&pagebar&uid=" + weiboUser.getUserId();
			List<WeiboMsg> apiMsgs = parseWBMsgFromApiHtmlData(client,
					homeRequest, weiboUser.getUserId(), weiboUser);
			if (apiMsgs.size() == 0) {
				logger.info(Thread.currentThread().getName()
						+ "获取微博失败,已经获取微博总数" + msgs.size());
				System.out.println(Thread.currentThread().getName()
						+ "获取微博失败,已经获取微博总数" + msgs.size());
				return apiMsgs.size();
			}
			msgs.addAll(apiMsgs);
			logger.info("当前用户userId：" + weiboUser.getUserId() + "获取第" + i
					+ "页1/3微博数" + apiMsgs.size() + ",共" + pageCount
					+ "页,已经获取微博总数" + msgs.size());
			System.out.println(Thread.currentThread().getName() + "当前用户："
					+ weiboUser.getScreenName() + " 获取第" + i + "页1/3微博数"
					+ apiMsgs.size() + ",共" + pageCount + "页,已经获取微博总数"
					+ msgs.size());
			try {// 说明已经找到了所有在最晚时间范围内的微博了
				if (msgs.get(msgs.size() - 1).getPublicTime()
						.before(sdf.parse(startTime))) {
					return msgs.size();
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
			if (i == pageCount && msgCount % 45 < 15) {
				break;// 最后一页小于15，则不需要lazyload
			}

			maxId = msgs.get(msgs.size() - 1).getId();
			// 第一次lazyload
			homeRequest = "http://weibo.com/aj/mblog/mbloglist?_wv=5&page=" + i
					+ "&count=15&max_id=" + maxId + "&pre_page=" + i
					+ "&end_id=&pagebar=0&uid=" + weiboUser.getUserId();
			apiMsgs = parseWBMsgFromApiHtmlData(client, homeRequest,
					weiboUser.getUserId(), weiboUser);
			msgs.addAll(apiMsgs);
			logger.info("当前用户userId：" + weiboUser.getUserId() + "获取第" + i
					+ "页2/3微博数" + apiMsgs.size() + ",共" + pageCount
					+ "页,已经获取微博总数" + msgs.size());
			System.out.println(Thread.currentThread().getName() + "当前用户："
					+ weiboUser.getScreenName() + " 获取第" + i + "页2/3微博数"
					+ apiMsgs.size() + ",共" + pageCount + "页,已经获取微博总数"
					+ msgs.size());
			try {// 说明已经找到了所有在最晚时间范围内的微博了
				if (msgs.get(msgs.size() - 1).getPublicTime()
						.before(sdf.parse(startTime))) {
					return msgs.size();
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
			if (i == pageCount && msgCount % 45 < 30) {
				break;// 最后一页小于30，则不需要二次lazyload
			}
			// 第二次lazyload
			homeRequest = "http://weibo.com/aj/mblog/mbloglist?_wv=5&page=" + i
					+ "&count=15&max_id=" + maxId + "&pre_page=" + i
					+ "&end_id=&pagebar=1&uid=" + weiboUser.getUserId();
			apiMsgs = parseWBMsgFromApiHtmlData(client, homeRequest,
					weiboUser.getUserId(), weiboUser);
			msgs.addAll(apiMsgs);
			logger.info("当前用户userId：" + weiboUser.getUserId() + "获取第" + i
					+ "页3/3微博数" + apiMsgs.size() + ",共" + pageCount
					+ "页,已经获取微博总数" + msgs.size());
			System.out.println(Thread.currentThread().getName() + "当前用户："
					+ weiboUser.getScreenName() + " 获取第" + i + "页3/3微博数"
					+ apiMsgs.size() + ",共" + pageCount + "页,已经获取微博总数"
					+ msgs.size());
			try {// 说明已经找到了所有在最晚时间范围内的微博了
				if (msgs.get(msgs.size() - 1).getPublicTime()
						.before(sdf.parse(startTime))) {
					return msgs.size();
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return msgs.size();
	}

	/**
	 * 获取微博用戶基本信息
	 * 
	 * @param httpClient
	 * @param userId
	 * @return
	 * @throws JSONException
	 */

	public WeiboUser getWeiboUserInfo(HttpClient httpClient, String userId) {
		String url = "http://weibo.com/" + userId + "/info";
		String entity = null;
		int count = 0;
		while (entity == null && count <= FileUtil.getFailureCount()) {
			logger.info("正在第" + ++count + "次获取" + url + "...");
			System.out.println(Thread.currentThread().getName() + "正在第" + count
					+ "次获取" + url + "...");
			entity = getRawHtml(httpClient, url);
		}
		if (entity == null) {// 5次还未获取，则放弃
			return null;
		}
		// FileUtil.saveToFile(entity, "personal.html", "utf-8");
		WeiboUser weiboUser = feed.getUserInfoFromRaw(entity);
		if (weiboUser == null) {// 解析错误
			return null;
		}
		weiboUser.setUserId(userId);
		return weiboUser;
	}

	/**
	 * 获取微博用户的关注列表和粉丝列表
	 * 
	 * @param httpClient
	 * @param weiboUser
	 * @return
	 */

	public WeiboUser getWeiboAllUserInfo(HttpClient httpClient,
			WeiboUser weiboUser) {
		String userId = weiboUser.getUserId();
		// int fansNum = weiboUser.getFansNum();
		int followNum = weiboUser.getFollowNum();
		// int fansPage = fansNum / 20 + 1;
		int followPage = followNum / 20 + 1;
		// if (fansPage > 50) {
		// fansPage = 50;
		// }
		if (followPage > 60) {
			followPage = 60;
		}
		// String fansUserIds = "";
		String followUserIds = "";
		// if (fansNum > 0) {
		// for (int i = 1; i <= fansPage; i++) {
		// String url = "http://weibo.com/" + userId + "/fans?page=" + i;
		// String entity = null;
		// int count = 0;
		// while (entity == null && count <= FileUtil.getFailureCount()) {
		// logger.info("正在第" + ++count + "次获取" + url + "." + "共"
		// + fansPage + "页...");
		// System.out.println(Thread.currentThread().getName() + "正在第"
		// + count + "次获取" + url + "." + "共" + fansPage
		// + "页...");
		// entity = getRawHtml(httpClient, url);
		// }
		// if (entity == null) {// 5次还未获取，则放弃
		// continue;
		// }
		// // FileUtil.saveToFile(entity, "fansPage.html", "utf-8");
		// fansUserIds += feed.getUserFansFromRaw(entity);
		// }
		// fansUserIds = fansUserIds.substring(1);
		// }

		if (followNum > 0) {
			for (int i = 1; i <= followPage; i++) {
				String url = "http://weibo.com/" + userId + "/follow?page=" + i;
				String entity = null;
				int count = 0;
				while (entity == null && count <= FileUtil.getFailureCount()) {
					logger.info("正在第" + ++count + "次获取" + url + "." + "共 "
							+ followPage + "页...");
					if (count > 1) {
						System.out.println(Thread.currentThread().getName()
								+ "正在第" + count + "次获取" + url + "." + "共 "
								+ followPage + "页...");
					}
					entity = getRawHtml(httpClient, url);
				}
				if (entity == null) {// 5次还未获取，则放弃
					continue;
				}
				// FileUtil.saveToFile(entity, "followPage.html", "utf-8");
				followUserIds += feed.getUserFollowFromRaw(entity);
			}
			followUserIds = followUserIds.substring(1);
		}
		// weiboUser.setFansUserId(fansUserIds);
		weiboUser.setFollowUserId(followUserIds);
		return weiboUser;
	}

	/**
	 * 返回html response
	 * 
	 * @param client
	 * @param personalUrl
	 * @return
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public String getRawHtml(HttpClient client, String personalUrl) {
		HttpGet getMethod = new HttpGet(personalUrl);
		String entityStr = null;
		InputStream in = null;
		try {
			HttpResponse response = client.execute(getMethod);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				in = entity.getContent(); // 之前没使用这个造成了大量异常抛出，只要是
			}
			entityStr = EntityUtils.toString(entity);
		} catch (IOException e) {
			logger.warn(e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return entityStr;
	}
}
