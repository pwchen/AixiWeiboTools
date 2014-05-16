package crawler.weibo.service;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import utils.FileUtils;
import crawler.weibo.service.fetcher.Fetcher;
import crawler.weibo.service.login.WeiboLoginHttpClientUtils;

public class CrawlMessageForwardTrack {

	static int count = 0;// 获取的转发ID数量
	static String saveUrl = "G:\\data\\forward\\";// 存盘的文件夹
	private static final Log logger = LogFactory
			.getLog(CrawlMessageForwardTrack.class);

	public static void main(String[] args) throws ClientProtocolException,
			IOException, JSONException {
		String baseUrl = "http://weibo.com/";
		String msgUrl = "1704832685/AfcEU2Yy0";// 待下载的list：
		String url = baseUrl + msgUrl;
		startCrawling(url);
		boolean flag = false;
		while (flag) {
			try {
				Thread.sleep(600000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			startCrawling(url);
		}
		// FileUtils.openFile(saveUrl + msgUrl);
	}

	/**
	 * 根据微博消息地址找到保存转发文件的地址
	 * 
	 * @param url
	 * @return
	 */
	private static String getSaveForwardUrl(String url) {
		long mainUid = getMainUid(url);
		String msgId = getMsgId(url);
		String saveForwardUrl = saveUrl + mainUid + "\\" + msgId
				+ "\\retweetedusers.csv";
		return saveForwardUrl;
	}

	/**
	 * 爬取转发用户及内容，保存文件
	 * 
	 * @param client
	 * @param url
	 */
	private static void startCrawling(String url) {
		String entity = Fetcher.getRawHtml(url + "?type=repost");
		logger.info("爬取的微博地址:" + url);
		if (entity == null) {
			logger.error("页面被删了，监控器退出");
			System.exit(0);
		}
		FileWriter fw = FileUtils.getFileWriter(getSaveForwardUrl(url), false);
		try {
			fw.write("id,name,publicTime,content\r\n");
			String PostHtmlentity = getFirstHTMLData(entity);
			Document document = Jsoup.parse(PostHtmlentity, "http://weibo.com");

			paseMainMsg(document, fw);
			int forwardNum = getForwardNum(document);
			logger.info("获得页面的转发数量为：" + forwardNum);

			paseForward(document, fw);
			int totalPageNum = (forwardNum % 20 == 0 ? forwardNum / 20
					: forwardNum / 20 + 1);
			if (totalPageNum > 1) {
				String pageUrl = getPageUrl(document);
				for (int i = 2; i <= totalPageNum; i++) {
					try {
						String temp = Fetcher.getRawHtml(pageUrl.substring(0,
								pageUrl.indexOf("page=") + 5) + i);
						if (temp == null) {
							for (int j = 0; j < 5; j++) {
								temp = Fetcher.getRawHtml(pageUrl.substring(
										0, pageUrl.indexOf("page=") + 5) + i);
								if (temp != null)
									break;
							}
						}
						entity = getNormalHTMLData(temp);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					document = Jsoup.parse(entity, "http://weibo.com");
					paseForward(document, fw);
					logger.info("爬取第" + i + "页评论，预计共" + totalPageNum
							+ "页.已获取转发：" + count);
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			FileUtils.closeFileWriter(fw);
		}
	}

	/**
	 * 解析作者及微博信息，写入作为转发文件的第一条
	 * 
	 * @param document
	 * @param fw
	 * @throws IOException
	 */
	private static void paseMainMsg(Document document, FileWriter fw)
			throws IOException {
		Element element = document.select("div.WB_detail").first();
		String id = element.select("div[node-type=feed_list]").first()
				.attr("tbinfo").substring(5);
		element = document.select("em").first();
		String name = element.attr("nick-name");
		String content = element.text();
		String publicTime = paseNomalPublicTime(document
				.select("a[node-type=feed_list_item_date]").first().text());
		Elements imgElements = document.select(
				"[node-type=feed_list_media_prev]").select("img.bigcursor");
		String imgStr = "";
		for (int i = 0; i < imgElements.size(); i++) {
			String s = imgElements.get(i).attr("src");
			s = s.replace("thumbnail", "large").replace("square", "large");
			imgStr += "," + s;
		}
		String wstr = id + "," + name + "," + publicTime + "," + content
				+ imgStr + "\r\n";
		fw.write(wstr);
	}

	/**
	 * 从url中获得微博ID
	 * 
	 * @param url
	 * @param mainUid
	 * @return
	 */
	private static String getMsgId(String url) {
		String msgId = url.substring(url.indexOf(getMainUid(url) + "/"));
		msgId = msgId.substring(msgId.indexOf("/") + 1);
		return msgId;
	}

	/**
	 * 根据微博URL获取微博主ID
	 * 
	 * @param url
	 * @return
	 */
	private static long getMainUid(String url) {
		url = url.substring(17);
		url = url.substring(0, url.indexOf("/"));
		return Long.parseLong(url);
	}

	/**
	 * 解析转发用户，并存盘：字段包括用户id、用户显示名、转发内容、转发时间
	 * 
	 * @param document
	 * @param fw
	 * @throws IOException
	 */
	private static void paseForward(Document document, FileWriter fw)
			throws IOException {
		Elements elements = document.select("dl");
		for (int i = 0; i < elements.size(); i++) {
			Element e = elements.get(i).select("dd>a").first();
			String name = e.attr("title");
			long id = Long.parseLong(e.attr("usercard").substring(3));
			String content = elements.get(i).select("dd>em").first().text();
			content = content.replace(",", "，").replace("\"", "“");
			String publicTime = elements.get(i).select(".S_txt2").text();
			// publicTime = publicTime.substring(1, publicTime.length() - 1);
			publicTime = paseNomalPublicTime(publicTime);
			String wstr = id + "," + name + "," + publicTime + "," + content
					+ "\r\n";
			fw.write(wstr);
			count++;
		}
	}

	/**
	 * 发布日期转换成统一的格式 yyyy-mm-dd hh:mm:ss
	 * 
	 * @param publicTime
	 * @return
	 */
	private static String paseNomalPublicTime(String publicTime) {
		Calendar d = Calendar.getInstance();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		if (publicTime.indexOf("分钟前") != -1) {
			int min = Integer.parseInt(publicTime.substring(0,
					publicTime.indexOf("分钟前")));
			publicTime = df.format(new Date(d.getTimeInMillis()
					- (min * 60 * 1000)));
		} else if (publicTime.indexOf("秒前") != -1) {
			int sec = Integer.parseInt(publicTime.substring(0,
					publicTime.indexOf("秒前")));
			publicTime = df
					.format(new Date(d.getTimeInMillis() - (sec * 1000)));
		} else if (publicTime.indexOf("-") == -1) {
			publicTime = publicTime.replace("今天", (d.get(Calendar.MONTH) + 1)
					+ "月" + d.get(Calendar.DAY_OF_MONTH) + "日");
			publicTime = d.get(Calendar.YEAR) + "-"
					+ publicTime.replace("月", "-").replace("日", "");
		}
		return publicTime;
	}

	/**
	 * 获取转发数量
	 * 
	 * @param document
	 * @return
	 */
	private static int getForwardNum(Document document) {
		Element element = document.select("[node-type=forward_counter]")
				.first();
		String str = element.text();
		int num = Integer.parseInt(str.substring(3, str.length() - 1));
		return num;
	}

	/**
	 * 生成后续页面URL
	 * 
	 * @param document
	 * @return
	 */
	private static String getPageUrl(Document document) {
		Element element = document.select("[action-type=commentFilter]>a")
				.first();
		String url = "http://weibo.com/aj/mblog/info/big?_wv=5&"
				+ element.attr("action-data");
		return url;
	}

	/**
	 * 解析第一页面的代码成Html
	 * 
	 * @param rawHTML
	 * @param pid
	 *            json的一个值，代表页面是首页还是个人主页还是别人的主页
	 * @return
	 * @throws JSONException
	 */
	private static String getFirstHTMLData(String rawHTML) {
		String htmlData = rawHTML
				.substring(rawHTML
						.indexOf("<script>FM.view({\"ns\":\"pl.content.weiboDetail.index"));
		htmlData = htmlData.substring(htmlData.indexOf("\"html\":\"") + 8,
				htmlData.indexOf("\"})</script>"));
		htmlData = htmlData.replace("\\/", "/").replace("\\n", "")
				.replace("\\t", "").replace("\\ \"", "\"")
				.replace("\\\"", "\"");
		return htmlData;
	}

	/**
	 * 解析其他页面的代码成Html
	 * 
	 * @param entity
	 * @return
	 * @throws JSONException
	 */
	public static String getNormalHTMLData(String entity) throws JSONException {
		JSONObject jsonObject;
		jsonObject = new JSONObject(entity);
		jsonObject = jsonObject.getJSONObject("data");
		entity = (jsonObject.toString().replace("\\\"", "\"").replace("\\\"",
				"\""));
		entity = entity.substring(entity.indexOf("},\"html\":\"") + 10)
				.replace("\\/", "/").replace("\\n", "").replace("\\t", "");
		return entity;
	}
}
