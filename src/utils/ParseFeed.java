package utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import crawler.weibo.dao.UserJdbcService;
import crawler.weibo.model.WeiboMsg;
import crawler.weibo.model.WeiboUser;

/**
 * 第一次以个人主页url抓取网页，分析后调用相应的接口，接口返回的是json
 * 
 * @author hoot
 * 
 */
public class ParseFeed {

	private static final Log logger = LogFactory.getLog(ParseFeed.class);
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	/**
	 * javascript中的正则STK.pageletM.view({"pid":"pl_content_homeFeed"开头,
	 * 以})</script>结尾,中间可以是任何字符串
	 */
	public static final String contentRegx = "<script>STK && STK.pageletM && STK.pageletM.view\\("
			+ "(\\{\"pid\":\"pl_content_homeFeed\"[\\s\\S]*?)" + "\\)</script>";

	/**
	 * 接受从response中读取的html，去除\/之类的字符，返回json中的html数据
	 * 
	 * @param rawHTML
	 * @param pid
	 *            json的一个值，代表页面是首页还是个人主页还是别人的主页
	 * @return
	 * @throws JSONException
	 */
	private String getNormalHTMLDataFromResponse(String rawHTML, String pid) {
		String contentRegx = "<script>STK && STK.pageletM && STK.pageletM.view\\("
				+ "(\\{\"pid\":\"" + pid + "\"[\\s\\S]*?)" + "\\)</script>";
		Pattern pattern = Pattern.compile(contentRegx);
		Matcher matcher = pattern.matcher(rawHTML);
		String htmlData = "";
		if (matcher.find()) {
			String htmlJson = matcher.group(1);
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(htmlJson.replace("\\/", "/"));
				htmlData = jsonObject.getString("html");
			} catch (JSONException e) {
				logger.error("JsonExctption", e);
			}

		}
		return htmlData;
	}

	/**
	 * 接受从response中读取的html，去除\/之类的字符，返回html字符串
	 * 
	 * @param rawHTML
	 * @return 首页的html数据
	 * @throws JSONException
	 */
	public String getHomeNormalHTMLData(String rawHTML) {
		String personId = "pl_content_homeFeed";
		return getNormalHTMLDataFromResponse(rawHTML, personId);
	}

	public String getHisNormalHTMLData(String rawHTML) {
		String personId = "pl_content_hisFeed";
		return getNormalHTMLDataFromResponse(rawHTML, personId);
	}

	public String getPl_leftNav_profilePersonal(String rawHTML) {
		String personId = "pl_leftNav_profilePersonal";
		return getNormalHTMLDataFromResponse(rawHTML, personId);
	}

	/**
	 * 接受从response中读取的html，去除\/之类的字符，返回html字符串
	 * 
	 * @param rawHTML
	 * @return 我的个人主页的html数据
	 * @throws JSONException
	 */
	public String getMyNormalHTMLData(String rawHtml) throws JSONException {
		String personId = "pl_profile_myInfo";
		return getNormalHTMLDataFromResponse(rawHtml, personId);
	}

	/**
	 * 获取他人微薄content html数据 接受从response中读取的html，去除\/之类的字符，返回html字符串
	 * 
	 * @param rawHTML
	 * @return 我的个人主页的html数据
	 * @throws JSONException
	 */
	public String getOtherNormalHTMLData(String rawHtml) throws JSONException {
		String personId = "pl_profile_hisInfo";
		return getNormalHTMLDataFromResponse(rawHtml, personId);
	}

	/**
	 * 通过jsoup来解析微博数量
	 * 
	 * @param normalHtml
	 * @return
	 * @throws JSONException
	 */
	public int parseFeedCount(String rawHtmlResponse) {
		// 从response中获取包含微博条数的html
		String personId = "pl_profile_photo";
		String normalHtml = getNormalHTMLDataFromResponse(rawHtmlResponse,
				personId);
		if ("".equals(normalHtml) || normalHtml == null) {
			normalHtml = getNormalHTMLDataFromResponse(rawHtmlResponse,
					"pl_content_litePersonInfo");
			Document document = Jsoup.parse(normalHtml, "http://weibo.com");
			int count = 0;
			try {
				count = Integer.parseInt(document.select("strong").last()
						.text());
			} catch (Exception e) {
				count = 0;
			}
			return count;
		}

		// jsop解析微博条数
		Document document = Jsoup.parse(normalHtml, "http://weibo.com");

		Elements elements = document.select("strong[node-type=weibo]");
		if (elements.size() == 1) {
			String weiboCount = elements.first().text();
			int count = Integer.parseInt(weiboCount);
			// logger.info("weiboCount：" + count);
			return count;
		}
		return -1;
	}

	/**
	 * 获取个人主页微博数据 从feed_list_item节点获得数据
	 * 
	 * @param element
	 * @return
	 * @throws ParseException
	 */
	public List<WeiboMsg> parsePersonalWeibo(Elements elements,
			WeiboUser weiboUser) {
		List<WeiboMsg> msgs = new ArrayList<WeiboMsg>();
		for (Element element : elements) {
			WeiboMsg msg = new WeiboMsg();
			// mid 用户ID 用户名 用户屏幕名
			msg.setId(element.attr("mid"));
			msg.setUserId(weiboUser.getUserId());
			msg.setScreenName(weiboUser.getScreenName());
			msg.setUserName(weiboUser.getUserName());
			// 个人发送的微博
			Element e = element.select("[node-type=feed_list_content]").first();
			if (e == null) {
				logger.info(element.html());
				return msgs;
			}
			// 微博内容
			msg.setContent(e.text());

			// 被at的user，没有id只有name
			String atUsers = "";
			Elements userElements = e.select("a[usercard]");
			for (Element ue : userElements) {
				atUsers += "," + ue.text();
			}
			if (!"".equals(atUsers))
				msg.setAtUsers(atUsers.substring(1));

			// 转发他人的微博
			Element forward = element.select(
					"div[node-type=feed_list_forwardContent]").first();
			Element handle = null;
			if (forward != null && !"".equals(forward)) {
				handle = element.select("div[class=WB_func clearfix]").last();
				Element forwarde = forward.select("div[mid]").first();
				if (forwarde == null) {
					continue;
				}
				msg.setRetweetedId(forwarde.attr("mid"));
				msg.setContent(msg.getContent()
						+ "<<"
						+ forward.select("div[node-type=feed_list_reason]")
								.first().text() + ">>");

				// msgs.add(feedReMsg(element));
			} else {
				handle = element.select("div[class=WB_func clearfix]").first();
			}

			if (handle == null) {
				// 企业用户
				String str2 = element
						.select("a[action-type=feed_list_forward]").first()
						.text();
				if ("转发".equals(str2) || str2 == null) {
					msg.setRetweetNum(0);
				} else {
					msg.setRetweetNum(Integer.parseInt(str2.substring(
							str2.indexOf("(") + 1, str2.indexOf(")"))));
				}
				String str3 = element
						.select("a[action-type=feed_list_comment]").first()
						.text();
				if ("评论".equals(str3) || str3 == null) {
					msg.setCommentNum(0);
				} else {
					msg.setCommentNum(Integer.parseInt(str3.substring(
							str3.indexOf("(") + 1, str3.indexOf(")"))));
				}
				// 发布时间 内容url 来源
				Element te = element.select("a[node-type=feed_list_item_date]")
						.first();
				String pTime = te.attr("title");
				try {
					msg.setPublicTime(sdf.parse(pTime));
				} catch (ParseException e1) {
					logger.error(e1);
				}
				msg.setContentUrl(te.attr("href"));
				try {
					msg.setSource(te.siblingElements().get(1).text());
				} catch (IndexOutOfBoundsException e2) {
					msg.setSource("未通过审核应用");
					// logger.error(e2);
					// logger.error(forward.outerHtml());
				}
			} else {
				// 赞数量，转发数量，评论数
				Elements likee = handle.select("a[action-type=feed_list_like]");

				if (likee == null || likee.size() <= 0) {
					msg.setLikeNum(0);
				} else {
					String str1 = likee.first().text();
					if ("".equals(str1)) {
						msg.setLikeNum(0);
					} else {
						msg.setLikeNum(Integer.parseInt(str1.substring(
								str1.indexOf("(") + 1, str1.indexOf(")"))));
					}
				}
				String str2 = handle.select("a[action-type=feed_list_forward]")
						.first().text();
				if ("转发".equals(str2) || str2 == null) {
					msg.setRetweetNum(0);
				} else {
					msg.setRetweetNum(Integer.parseInt(str2.substring(
							str2.indexOf("(") + 1, str2.indexOf(")"))));
				}
				String str3 = handle.select("a[action-type=feed_list_comment]")
						.first().text();
				if ("评论".equals(str3) || str3 == null) {
					msg.setCommentNum(0);
				} else {
					msg.setCommentNum(Integer.parseInt(str3.substring(
							str3.indexOf("(") + 1, str3.indexOf(")"))));
				}
				// 发布时间 内容url 来源
				Element te = handle.select("a[node-type=feed_list_item_date]")
						.first();
				String pTime = te.attr("title");

				try {
					msg.setPublicTime(sdf.parse(pTime));
				} catch (ParseException e1) {
					logger.error(pTime);
					logger.error(e1);
				}
				msg.setContentUrl(te.attr("href"));
				msg.setSource(te.siblingElements().get(1).text());
			}
			// 图片
			Element forwardme = element.select(
					"[action-type=feed_list_media_img]").first();
			if (forwardme != null && !"".equals(forwardme)) {
				String imUrl = forwardme.select("img").first().attr("src")
						.replace("thumbnail", "bmiddle");
				msg.setPictureUrl(imUrl);
			}
			// 视频
			forwardme = element.select("[action-type=feed_list_media_video]")
					.first();
			if (forwardme != null && !"".equals(forwardme)) {
				msg.setVideoUrl(forwardme.attr("href"));
			}
			// 音乐
			forwardme = element.select("[action-type=feed_list_media_music]")
					.first();
			if (forwardme != null && !"".equals(forwardme)) {
				msg.setVoiceUrl(forwardme.attr("href"));
			}

			// 公益
			forwardme = element.select("a[action-type=feed_list_third_rend]")
					.first();
			if (forwardme != null && !"".equals(forwardme)) {
				if ("微公益".equals(forwardme.select("span").attr("title"))) {
					String gongyiUrl = forwardme.attr("action-data");
					gongyiUrl = gongyiUrl.substring(
							gongyiUrl.indexOf("short_url=") + 10,
							gongyiUrl.indexOf("&full_url="));
					msg.setGongyiUrl(gongyiUrl);
				}
			}
			// CREATE_TIME UPDATE_TIME
			Date nowDate = new Date();
			msg.setCreateTime(nowDate);
			msg.setUpdateTime(nowDate);
			MsgJdbcService.getInstance().saveMsg(msg);
			msgs.add(msg);
		}
		return msgs;
	}

	private WeiboMsg feedReMsg(Element element) {
		Element forward = element.select(
				"div[node-type=feed_list_forwardContent]").first();
		Element forwarde = forward.select("div[mid]").first();
		WeiboMsg reMsg = new WeiboMsg();
		// 微博MID
		reMsg.setId(forwarde.attr("mid"));

		// 屏幕名，用户名，用户ID，微博内容
		Element forwarde1 = forward.select("a[node-type=feed_list_originNick]")
				.first();
		reMsg.setScreenName(forwarde1.attr("title"));
		reMsg.setUserName(forwarde1.attr("href").substring(1));
		reMsg.setUserId(forwarde1.attr("usercard").split("=")[1]);
		reMsg.setContent(forward.select("div[node-type=feed_list_reason]")
				.first().text());
		// 赞数量，转发数量，评论数
		Elements forwardes = forwarde.select("a");
		String str1 = forwardes.get(0).text();
		if ("".equals(str1) || str1 == null) {
			reMsg.setLikeNum(0);
		} else {
			reMsg.setLikeNum(Integer.parseInt(str1.substring(
					str1.indexOf("(") + 1, str1.indexOf(")"))));
		}
		String str2 = forwardes.get(1).text();
		if ("转发".equals(str2) || str2 == null) {
			reMsg.setRetweetNum(0);
		} else {
			reMsg.setRetweetNum(Integer.parseInt(str2.substring(
					str2.indexOf("(") + 1, str2.indexOf(")"))));
		}
		String str3 = forwardes.get(2).text();
		if ("评论".equals(str3) || str3 == null) {
			reMsg.setCommentNum(0);
		} else {
			reMsg.setCommentNum(Integer.parseInt(str3.substring(
					str3.indexOf("(") + 1, str3.indexOf(")"))));
		}
		// 内容url， 发布日期 ，发布来源
		Element datee = forward.select("a[node-type=feed_list_item_date]")
				.first();
		reMsg.setContentUrl(datee.attr("href"));
		String pTime = datee.attr("title");
		try {
			reMsg.setPublicTime(sdf.parse(pTime));
		} catch (ParseException e1) {
			logger.error(e1);
		}
		try {
			reMsg.setSource(datee.siblingElements().get(1).text());
		} catch (IndexOutOfBoundsException e2) {
			reMsg.setSource("未通过审核应用");
			// logger.error(e2);
			// logger.error(forward.outerHtml());
		}
		// 被at的user，没有id只有name
		String fAtUsers = "";
		Elements userElements = forward.select("a[usercard]");
		for (Element ue : userElements) {
			fAtUsers += "," + ue.text();
		}
		reMsg.setAtUsers(fAtUsers);
		// 图片
		Element forwardme = forward.select("[action-type=feed_list_media_img]")
				.first();
		if (forwardme != null && !"".equals(forwardme)) {
			String imUrl = forwardme.select("img").first().attr("src")
					.replace("thumbnail", "bmiddle");
			reMsg.setPictureUrl(imUrl);
		}
		// 视频
		forwardme = forward.select("[action-type=feed_list_media_video]")
				.first();
		if (forwardme != null && !"".equals(forwardme)) {
			reMsg.setVideoUrl(forwardme.attr("href"));
		}
		// 音乐
		forwardme = forward.select("[action-type=feed_list_media_music]")
				.first();
		if (forwardme != null && !"".equals(forwardme)) {
			reMsg.setVoiceUrl(forwardme.attr("href"));
		}
		// CREATE_TIME UPDATE_TIME
		Date nowDate = new Date();
		reMsg.setCreateTime(nowDate);
		reMsg.setUpdateTime(nowDate);
		MsgJdbcService.getInstance().saveMsg(reMsg);
		return reMsg;
	}

	/**
	 * 返回包含weibo内容的Elements
	 * 
	 * @param longHTMLStr
	 * @return
	 */
	public Elements getWeiboContentElements(String longHTMLStr) {
		// File input = new File("src/weibo_data.html");
		Document doc = Jsoup.parse(longHTMLStr, "http://weibo.com");
		// 获取微薄数目，每条是
		Elements divElement = doc.select("[action-type=feed_list_item]");// "div[diss-data=group_source=group_all]"
		// logger.info(divElement.size());
		return divElement;
	}

	public WeiboUser getUserInfoFromRaw(String entity) {
		WeiboUser weiboUser = new WeiboUser();

		/*** pl_profile_hisInfo ****/
		String pl_profile_hisInfo = getNormalHTMLDataFromResponse(entity,
				"pl_profile_hisInfo");
		// FileUtil.saveToFile(pl_profile_hisInfo, "pl_profile_hisInfo.html",
		// "utf-8");
		Document document = Jsoup.parse(pl_profile_hisInfo, "http://weibo.com");
		// 屏幕名
		Element screenName = document.select(".name").first();
		if (screenName != null) {
			weiboUser.setScreenName(screenName.text());
		} else {
			logger.error(Thread.currentThread().getName() + "解析页面出错！");
			System.out.println(Thread.currentThread().getName()
					+ Thread.currentThread().getName() + "解析页面出错！");
			return null;
		}
		// 达人
		Element daren = document.select("i[node-type=daren]").first();
		if (daren != null) {
			weiboUser.setDaren("1");
		} else {
			weiboUser.setDaren("0");
		}
		// 认证
		Element verify = document.select(
				".icon_bed>a[href=http://verified.weibo.com/verify]").first();
		if (verify != null) {
			weiboUser.setIsVerified("1");
			weiboUser.setVerifyInfo(verify.select("i").attr("title"));
		} else {
			weiboUser.setIsVerified("0");
		}

		// 会员
		Element vip = document.select(
				".icon_bed>a[href=http://vip.weibo.com/personal?from=main]")
				.first();
		if (vip != null
				&& vip.select("i").first().className()
						.equals("W_ico16 ico_member")) {
			weiboUser.setVip("1");
		} else {
			weiboUser.setVip("0");
		}

		// 等级
		Element level = document.select(".icon_bed[node-type=level]").first();
		if (level != null) {
			String levelTitle = level.getElementsByClass("W_level_num").first()
					.attr("title");
			weiboUser.setDengji(levelTitle.substring(5));
		}

		// 简介
		Element description = document.select(".pf_intro>span").first();
		if (description != null) {
			weiboUser.setDescription(description.attr("title"));
		}

		// 标签
		Elements tags = document.select("div[node-type=infoTag]").select("a");
		if (tags.size() > 0) {
			weiboUser.setTag(tags.select("span").text());
		}

		/*** pl_profile_photo ***/
		String pl_profile_photo = getNormalHTMLDataFromResponse(entity,
				"pl_profile_photo");
		// FileUtil.saveToFile(pl_profile_photo, "pl_profile_photo.html",
		// "utf-8");
		document = Jsoup.parse(pl_profile_photo, "http://weibo.com");

		// 头像url
		Element profileImageUrl = document.select(".pf_head_pic").first();
		if (profileImageUrl != null) {
			weiboUser.setProfileImageUrl(profileImageUrl.select("img").attr(
					"src"));
		}
		// 关注
		Element followNum = document.select("strong[node-type=follow]").first();
		if (followNum != null) {
			weiboUser.setFollowNum(Integer.parseInt(followNum.text()));
		}

		// 粉丝
		Element fansNum = document.select("strong[node-type=fans]").first();
		if (fansNum != null) {
			weiboUser.setFansNum(Integer.parseInt(fansNum.text()));
		}

		// 微博
		Element weiboNum = document.select("strong[node-type=weibo]").first();
		if (weiboNum != null) {
			weiboUser.setMessageNum(Integer.parseInt(weiboNum.text()));
		}

		Element userName = document.select("[name=profile_tab]").first();
		if (userName != null) {
			String hrefScr = userName.attr("href");
			if (hrefScr.indexOf("/u/") != -1) {
				weiboUser
						.setUserName(hrefScr.substring(3, hrefScr.indexOf("?")));
			} else {
				weiboUser
						.setUserName(hrefScr.substring(1, hrefScr.indexOf("?")));
			}

		}

		/*** pl_profile_infoBase ***/

		String pl_profile_infoBase = getNormalHTMLDataFromResponse(entity,
				"pl_profile_infoBase");
		// FileUtil.saveToFile(pl_profile_infoBase, "pl_profile_infoBase.html",
		// "utf-8");
		document = Jsoup.parse(pl_profile_infoBase, "http://weibo.com");

		// 所在地
		Element region = document.select(":contains(所在地)+div").first();
		if (region != null) {
			weiboUser.setRegion(region.text());
		}

		// 性别
		Element sex = document.select(":contains(性别)+div").first();
		if (sex != null) {
			weiboUser.setSex(sex.text());
		}

		// 生日
		Element birthday = document.select(":contains(生日)+div").first();
		if (birthday != null) {
			weiboUser.setBirthday(birthday.text());
		}

		// 博客
		Element blog = document.select(":contains(博客)+div>a").first();
		if (blog != null) {
			weiboUser.setBlog(blog.text());
		}

		// 邮箱
		Element email = document.select(":contains(邮箱)+div>a").first();
		if (email != null) {
			weiboUser.setEmail(email.text());
		}
		// QQ
		Element qq = document.select(":contains(QQ)+div>a").first();
		if (qq != null) {
			weiboUser.setQq(qq.text());
		}
		// MSN
		Element msn = document.select(":contains(MSN)+div>a").first();
		if (msn != null) {
			weiboUser.setMsn(msn.text());
		}

		// 个性域名
		Element domain = document.select(":contains(个性域名)+div>a").first();
		if (domain != null) {
			weiboUser.setDomain(domain.text());
		}

		/*** pl_profile_infoEdu ***/

		String pl_profile_infoEdu = getNormalHTMLDataFromResponse(entity,
				"pl_profile_infoEdu");
		// FileUtil.saveToFile(pl_profile_infoEdu, "pl_profile_infoEdu.html",
		// "utf-8");
		document = Jsoup.parse(pl_profile_infoEdu, "http://weibo.com");
		// 教育
		Elements eduInfos = document.select(".con");
		if (eduInfos.size() > 0) {
			String eduInfo = "";
			for (int i = 0; i < eduInfos.size(); i++) {
				eduInfo += "," + eduInfos.get(i).text();
			}
			weiboUser.setEducationInfo(eduInfo.substring(1));
		}

		/*** pl_profile_infoCareer ***/
		String pl_profile_infoCareer = getNormalHTMLDataFromResponse(entity,
				"pl_profile_infoCareer");
		// FileUtil.saveToFile(pl_profile_infoCareer,
		// "pl_profile_infoCareer.html", "utf-8");
		document = Jsoup.parse(pl_profile_infoCareer, "http://weibo.com");
		// 职业
		Elements careerInfos = document.select(".con");
		if (careerInfos.size() > 0) {
			String carreerInfo = "";
			for (int i = 0; i < careerInfos.size(); i++) {
				carreerInfo += "," + careerInfos.get(i).text();
			}
			weiboUser.setCareerInfo(carreerInfo.substring(1));
		}

		return weiboUser;
	}

	public String getUserFansFromRaw(String entity) {
		String pl_relation_hisFans = getNormalHTMLDataFromResponse(entity,
				"pl_relation_hisFans");
		// FileUtil.saveToFile(pl_relation_hisFans, "pl_relation_hisFans.html",
		// "utf-8");
		Document document = Jsoup
				.parse(pl_relation_hisFans, "http://weibo.com");
		Elements fans = document.select("li[action-type=itemClick]");
		String fansStr = "";
		for (int i = 0; i < fans.size(); i++) {
			String userId = fans.get(i).select(".name>a").attr("usercard")
					.substring(3);
			fansStr += "," + userId;
			UserJdbcService.getInstance().addUserIDfromQueue(userId);// 加入队列
		}
		return fansStr;
	}

	public String getUserFollowFromRaw(String entity) {
		String pl_relation_hisFollow = getNormalHTMLDataFromResponse(entity,
				"pl_relation_hisFollow");
		// FileUtil.saveToFile(pl_relation_hisFollow,
		// "pl_relation_hisFollow.html", "utf-8");
		Document document = Jsoup.parse(pl_relation_hisFollow,
				"http://weibo.com");
		Elements follows = document.select("li[action-type=itemClick]");
		String followsStr = "";
		for (int i = 0; i < follows.size(); i++) {
			String userId = follows.get(i).select(".name>a").attr("usercard")
					.substring(3);
			followsStr += "," + userId;
			UserJdbcService.getInstance().addUserIDfromQueue(userId);// 加入队列
		}
		return followsStr;
	}

	public int getSearchMsgCount(String longHTMLStr) {
		Document doc = Jsoup.parse(longHTMLStr, "http://weibo.com");
		// 获取微薄数目，每条是
		Element element = doc.select("[class=W_bread_nav]").first()
				.select("strong").first();
		String searchResultStr = element.text();
		System.out.println(searchResultStr);
		return Integer.parseInt(searchResultStr);
	}
}
