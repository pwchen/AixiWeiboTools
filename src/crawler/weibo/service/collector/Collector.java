package crawler.weibo.service.collector;

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
import utils.FileUtils;
import crawler.weibo.service.login.WeiboLoginHttpClientUtils;

public class Collector {
	private static final Log logger = LogFactory.getLog(Collector.class);

	public static void main(String args[]) {
		System.out.println(getRawHtml("http://weibo.com/u/5061880888"));
	}

	/**
	 * 获取制定url的网页内容
	 * 
	 * @param client
	 * @param personalUrl
	 * @return
	 */
	public static String getRawHtml(String url) {
		HttpClient client = WeiboLoginHttpClientUtils.getLoginhttpClient();
		HttpGet getMethod = new HttpGet(url);
		String entityStr = null;
		InputStream in = null;
		int count = 0;
		boolean ex = true;// 出异常
		int failureCount = CrawlerContext.getContext().getFailureNumber();
		while (count++ < CrawlerContext.getContext().getFailureNumber() && ex) {
			ex = false;
			try {
				HttpResponse response = client.execute(getMethod);
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					in = entity.getContent(); // 之前没使用这个造成了大量异常抛出
				}
				entityStr = EntityUtils.toString(entity, "utf-8");
			} catch (IOException e) {
				ex = true;
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
			if (entityStr.indexOf("抱歉，网络繁忙") != -1) {
				client = WeiboLoginHttpClientUtils.changeLoginAccount();
				logger.error("抱歉，网络繁忙:" + url);
				ex = true;
			} else if (entityStr.indexOf("正确输入验证码答案") != -1) {
				WeiboLoginHttpClientUtils.expireClient = true;//账号异常，需要换号了
				client = WeiboLoginHttpClientUtils.changeLoginAccount();
				logger.error("正确输入验证码答案:" + url);
				ex = true;
			} else if (entityStr
					.indexOf("æ°æµªå¾®å-éæ¶éå°åäº«èº«è¾¹çæ°é²äºå¿") != -1) {
				FileUtils.saveToFile(entityStr, "tempuft.html", "utf-8");
				FileUtils.saveToFile(entityStr, "tempgbk.html", "gbk");
				logger.error("乱码异常:æ°æµªå¾®å-éæ¶éå°åäº«èº«è¾¹çæ°é²äºå¿"
						+ url);
				try {
					Thread.sleep(6000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return entityStr;
			} else if (entityStr.indexOf("您当前访问的帐号异常") != -1) {
				WeiboLoginHttpClientUtils.expireClient = true;
				client = WeiboLoginHttpClientUtils.changeLoginAccount();
				logger.error("您当前访问的帐号异常:" + url);
				ex = true;
			} else if (entityStr
					.indexOf("The server returned an invalid or incomplete response") != -1) {
				WeiboLoginHttpClientUtils.expireClient = true;
				client = WeiboLoginHttpClientUtils.changeLoginAccount();
				logger.error("502 Bad Gateway,The server returned an invalid or incomplete response:"
						+ url);
				ex = true;
			} else if (entityStr.indexOf("页面地址有误") != -1) {
				logger.warn("页面地址有误，或者该页面不存在:" + url);
				return null;
			}
		}
		if (count > failureCount) {
			logger.error("超时次数超过最大次数:" + failureCount + "放弃此次请求。");
		} else if (entityStr == null) {
			logger.error("未知原因:" + url);
		}
		return entityStr;
	}
}
