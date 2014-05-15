package crawler.weibo.service.parser;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import utils.FileUtils;
import crawler.weibo.model.WeiboUser;
import crawler.weibo.service.collector.Collector;
import crawler.weibo.service.login.WeiboLoginHttpClientUtils;

/**
 * 第一次以个人主页url抓取网页，分析后调用相应的接口，接口返回的是json
 * 
 * @author hoot
 * 
 */
public class SimpleUserParser {

	private static final Log logger = LogFactory.getLog(SimpleUserParser.class);
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	/**
	 * 爬取用户精简页面，提取用户简单基本信息
	 * 
	 * @param httpClient
	 * @param userId
	 * @return
	 */
	public static WeiboUser getSimpleWeiboUserInfo(String userId) {
		String url = "http://weibo.com/aj/user/newcard?type=1&id=" + userId
				+ "&callback=STK_" + (new Date().getTime() * 1000);
		String entity = null;
		entity = Collector.getRawHtml(url);
		if (entity == null) {
			return null;
		}
		WeiboUser weiboUser = parseSimpleUser(entity);
		return weiboUser;
	}

	private static WeiboUser parseSimpleUser(String entity) {
		String html = entity.replace("\\/", "/");
		if (html.indexOf("{\"code\"") == -1)
			return null;
		html = html.substring(html.indexOf("{\"code\""),
				html.indexOf("})}catch") + 1);
		try {
			JSONObject jo = new JSONObject(html);
			if (jo.getString("code").equals("100001")) {
				logger.warn(jo.get("msg"));
				return null;
			}
			html = jo.getString("data");
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		if (html == null || "".equals(html)) {
			FileUtils.saveToFile(entity, "parseSimpleUserentity.html", "utf-8");
		}
		WeiboUser wu = new WeiboUser();
		Document document = Jsoup.parse(html, "http://weibo.com");
		Element element = document.select("div>div>dl>dd").first();
		String userId = element.select("p>a").first().attr("uid");
		wu.setUserId(userId);
		String screenName = element.select("a").first().attr("title");
		wu.setScreenName(screenName);
		try {
			String str = document.select("div>ul.userdata").first().text()
					.replace("万", "0000");
			String[] strs = str.split(" \\| ");
			int followNum = Integer.parseInt(strs[0].substring(2));
			int fansNum = Integer.parseInt(strs[1].substring(2));
			int msgNum = Integer.parseInt(strs[2].substring(2));
			wu.setFollowNum(followNum);
			wu.setFansNum(fansNum);
			wu.setMessageNum(msgNum);
		} catch (Exception e) {
			e.printStackTrace();
			FileUtils.saveToFile(document.html(), "document.html", "utf-8");
		}
		return wu;
	}
}
