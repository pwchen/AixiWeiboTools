package crawler.weibo.service.parser;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import utils.FileUtils;
import crawler.weibo.model.WeiboUser;
import crawler.weibo.service.fetcher.Fetcher;

/**
 * 第一次以个人主页url抓取网页，分析后调用相应的接口，接口返回的是json
 * 
 * @author Joe
 * 
 */
public class UserParser {

	private static final Log logger = LogFactory.getLog(UserParser.class);
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	/**
	 * 接受从response中读取的html，去除\/之类的字符，返回json中的html数据
	 * 
	 * @param rawHTML
	 * @param pid
	 *            json的一个值，代表页面是首页还是个人主页还是别人的主页
	 * @return
	 * @throws JSONException
	 */
	private String getNormalHTMLDataFromResponse(String entity, String domid) {
		String rawHTML = null;
		try {
			rawHTML = entity.substring(entity.indexOf(domid));
			rawHTML = rawHTML.substring(rawHTML.indexOf("\"html\":\"") + 8,
					rawHTML.indexOf("\"})</script>"));
			rawHTML = rawHTML.replace("\\r", "").replace("\\n", "")
					.replace("\\t", "").replace("\\/", "/").replace("\\\"", "");
		} catch (StringIndexOutOfBoundsException e) {
			logger.error(domid + ":" + e.getMessage());
			FileUtils.saveToFile(entity, "domid_entity.html", "utf-8");
		}
		return rawHTML;
	}

	/**
	 * 爬取用户页面，提取用户基本信息
	 * 
	 * @param httpClient
	 * @param userId
	 * @return
	 */
	public WeiboUser getWeiboUserInfo(String userId) {
		String url = "http://weibo.com/" + userId + "/info";
		String entity = null;
		entity = Fetcher.getRawHtml(url);
		if (entity == null) {
			return null;
		}
		WeiboUser weiboUser = parseUserInfoDatabyModel(entity);
		if (weiboUser == null) {// 解析错误
			logger.error("解析" + userId + "失败！");
			return null;
		}
		weiboUser.setUserId(userId);
		return weiboUser;
	}

	/**
	 * 判断页面类型并解析各类型页面特征，选择相应的页面解析方法
	 * 
	 * @param entity
	 * @return
	 */
	public WeiboUser parseUserInfoDatabyModel(String entity) {
		String domid1 = "\"domid\":\"" + "Pl_Official_Header__1" + "\"";
		String domid2 = "\"domid\":\"" + "Pl_Core_Header__1" + "\"";
		String domid3 = "\"pid\":\"pl_profile_hisInfo\"";
		if (entity.indexOf(domid1) != -1) {
			return paseUserInfo_PlOfficialHeader(entity);
		} else if (entity.indexOf(domid2) != -1) {
			return paseUserInfo_PlCoreHeader(entity);
		} else if (entity.indexOf(domid3) != -1) {
			return paseUserInfo_plprofilehisInfo(entity);
		} else {
			String entityFileName = "entity" + new Date().getTime() + ".html";
			if (entity.indexOf("抱歉，网络繁忙") != -1) {
				logger.warn("抱歉，网络繁忙!稍后再试！");
				return null;
			}
			if (entity.indexOf("页面地址有误") != -1) {
				logger.warn("抱歉，你访问的页面地址有误，或者该页面不存在");
				return null;
			}
			FileUtils.saveToFile(entity, entityFileName, "utf-8");
			logger.error("日啊，又有新的模板！！！！！！！！！！！源文件保存至" + entityFileName);
			return null;
		}
	}

	/**
	 * 根据模型Pl_Official_Header__1解析用户信息
	 * 
	 * @param entity
	 * @return
	 */
	private WeiboUser paseUserInfo_PlOfficialHeader(String entity) {
		String domid = "\"domid\":\"" + "Pl_Official_Header__1" + "\"";
		String plOfficialHeaderHtml = getNormalHTMLDataFromResponse(entity,
				domid);

		WeiboUser weiboUser = new WeiboUser();
		Document document = Jsoup.parse(plOfficialHeaderHtml,
				"http://weibo.com");
		// 屏幕名
		Element screenName = document.select(".name").first();

		if (screenName != null) {
			weiboUser.setScreenName(screenName.text());
		} else {
			String entityFileName = "entity" + new Date().getTime() + ".html";
			FileUtils.saveToFile(entity, entityFileName, "utf-8");
			logger.error("解析页面出错！保存文件名：" + entityFileName);
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

		domid = "\"domid\":\"" + "Pl_Official_LeftInfo__14" + "\"";
		if (entity.indexOf(domid) == -1) {
			return weiboUser;
		}
		/** Pl_Official_LeftInfo__14 **/
		String plOfficialLeftInfoHtml = getNormalHTMLDataFromResponse(entity,
				domid);
		// PropertyUtils.saveToFile(pl_profile_infoBase,
		// "pl_profile_infoBase.html",
		// "utf-8");

		document = Jsoup.parse(plOfficialLeftInfoHtml, "http://weibo.com");

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

		// 教育
		Elements eduInfos = document.select(".con");
		if (eduInfos.size() > 0) {
			String eduInfo = "";
			for (int i = 0; i < eduInfos.size(); i++) {
				eduInfo += "," + eduInfos.get(i).text();
			}
			weiboUser.setEducationInfo(eduInfo.substring(1));
		}

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

	/**
	 * 解析模型Pl_Core_Header__1的用户信息
	 * 
	 * @param entity
	 * @return
	 */
	private WeiboUser paseUserInfo_PlCoreHeader(String entity) {
		String domid = "\"domid\":\"" + "Pl_Core_Header__1" + "\"";
		String plCoreHeaderHtml = getNormalHTMLDataFromResponse(entity, domid);
		// PropertyUtils.saveToFile(plCoreHeaderHtml, "PlCoreHeader.html",
		// "utf-8");

		WeiboUser weiboUser = new WeiboUser();
		Document document = Jsoup.parse(plCoreHeaderHtml, "http://weibo.com");
		// 屏幕名
		Element screenName = document.select(".username>#place>strong").first();
		if (screenName != null) {
			weiboUser.setScreenName(screenName.text());
		} else {
			String entityFileName = "entity" + new Date().getTime() + ".html";
			FileUtils.saveToFile(entity, entityFileName, "utf-8");
			logger.error("解析页面出错！保存文件名：" + entityFileName);
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
				"a[href=http://verified.weibo.com/verify]").first();
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
		Element description = document.select(".moreinfo").first();
		if (description != null) {
			weiboUser.setDescription(description.text());
		}

		// 标签
		Elements tags = document.select("div[node-type=infoTag]").select("a");
		if (tags.size() > 0) {
			weiboUser.setTag(tags.select("span").text());
		}

		// 头像url
		Element profileImageUrl = document.select(".pf_head_pic").first();
		if (profileImageUrl != null) {
			weiboUser.setProfileImageUrl(profileImageUrl.select("img").attr(
					"src"));
		}
		// 关注
		String userAtten = document.select(".user_atten").first().text();
		if (userAtten != null) {
			int followNum = Integer.parseInt(userAtten.substring(0,
					userAtten.indexOf("关注")));
			weiboUser.setFollowNum(followNum);
			int fansNum = Integer.parseInt(userAtten.substring(
					userAtten.indexOf("关注") + 3, userAtten.indexOf("粉丝")));
			weiboUser.setFansNum(fansNum);
			int messageNum = Integer.parseInt(userAtten.substring(
					userAtten.indexOf("粉丝") + 3, userAtten.indexOf("微博")));
			weiboUser.setMessageNum(messageNum);
		}

		Element userName = document.select(".username>#place>span").first();
		if (userName != null) {
			String hrefScr = userName.text();
			if (hrefScr.indexOf("/u/") != -1) {
				weiboUser.setUserName(hrefScr.substring(19));
			} else {
				weiboUser.setUserName(hrefScr.substring(17));
			}

		}

		/** plCoreLeftPicTextUserHtml 这里还有问题，以后再解析 **/
		domid = "\"domid\":\"" + "Pl_Core_LeftPicTextUser__21" + "\"";
		if (entity.indexOf(domid) == -1) {
			return weiboUser;
		}
		String plCoreLeftPicTextUserHtml = getNormalHTMLDataFromResponse(
				entity, domid);
		document = Jsoup.parse(plCoreLeftPicTextUserHtml, "http://weibo.com");

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

		// 教育
		Elements eduInfos = document.select(".con");
		if (eduInfos.size() > 0) {
			String eduInfo = "";
			for (int i = 0; i < eduInfos.size(); i++) {
				eduInfo += "," + eduInfos.get(i).text();
			}
			weiboUser.setEducationInfo(eduInfo.substring(1));
		}

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

	/**
	 * 根据pl_profile_hisInfo模型解析用户页面
	 * 
	 * @param entity
	 * @return
	 */
	private WeiboUser paseUserInfo_plprofilehisInfo(String entity) {
		String domid = "\"pid\":\"pl_profile_hisInfo\"";
		String plprofilehisInfoHtml = getNormalHTMLDataFromResponse(entity,
				domid);
		// PropertyUtils.saveToFile(plprofilehisInfoHtml,
		// "plprofilehisInfo.html",
		// "utf-8");

		WeiboUser weiboUser = new WeiboUser();
		Document document = Jsoup.parse(plprofilehisInfoHtml,
				"http://weibo.com");
		// 屏幕名
		Element screenName = document.select(".name").first();
		if (screenName != null) {
			weiboUser.setScreenName(screenName.text());
		} else {
			String entityFileName = "entity" + new Date().getTime() + ".html";
			FileUtils.saveToFile(entity, entityFileName, "utf-8");
			logger.error("解析页面出错！保存文件名：" + entityFileName);
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
		domid = "\"pid\":\"pl_profile_photo\"";
		String plprofilephotoHtml = getNormalHTMLDataFromResponse(entity, domid);
		// PropertyUtils.saveToFile(pl_profile_photo, "pl_profile_photo.html",
		// "utf-8");
		document = Jsoup.parse(plprofilephotoHtml, "http://weibo.com");

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
		domid = "\"pid\":\"pl_profile_infoBase\"";
		String plprofileinfoBaseHtml = getNormalHTMLDataFromResponse(entity,
				domid);
		// PropertyUtils.saveToFile(pl_profile_infoBase,
		// "pl_profile_infoBase.html",
		// "utf-8");
		document = Jsoup.parse(plprofileinfoBaseHtml, "http://weibo.com");

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

		domid = "\"pid\":\"pl_profile_infoBase\"";
		String plprofileinfoEduHtml = getNormalHTMLDataFromResponse(entity,
				domid);
		// PropertyUtils.saveToFile(pl_profile_infoEdu,
		// "pl_profile_infoEdu.html",
		// "utf-8");
		document = Jsoup.parse(plprofileinfoEduHtml, "http://weibo.com");
		// 教育
		Elements eduInfos = document.select(".con");
		if (eduInfos.size() > 0) {
			String eduInfo = "";
			for (int i = 0; i < eduInfos.size(); i++) {
				eduInfo += "," + eduInfos.get(i).text();
			}
			weiboUser.setEducationInfo(eduInfo.substring(1));
		}

		domid = "\"pid\":\"pl_profile_infoCareer\"";
		/*** pl_profile_infoCareer ***/
		String plprofileinfoCareerHtml = getNormalHTMLDataFromResponse(entity,
				domid);
		// PropertyUtils.saveToFile(pl_profile_infoCareer,
		// "pl_profile_infoCareer.html", "utf-8");
		document = Jsoup.parse(plprofileinfoCareerHtml, "http://weibo.com");
		// 职业
		Elements careerInfos = document.select(".con");
		if (careerInfos.size() > 0) {
			String carreerInfo = "";
			for (int i = 0; i < careerInfos.size(); i++) {
				carreerInfo += "," + careerInfos.get(i).text();
			}
			weiboUser.setCareerInfo(carreerInfo.substring(1));
		}
		// System.out.println(weiboUser);
		return weiboUser;
	}

	/**
	 * 解析用户的关注列表
	 * 
	 * @param entity
	 * @return
	 */
	public String getUserFollowFromRaw(String entity) {
		String domid = "\"pid\":\"pl_relation_hisFollow\"";
		String domid0 = "\"pid\":\"pl_relation_hisFans\"";
		String domid1 = "\"domid\":\"Pl_Official_LeftHisRelation";

		String followsStr = "";
		String plOfficialLeftHisRelationHtml = null;
		if (entity.indexOf(domid) != -1) {
			plOfficialLeftHisRelationHtml = getNormalHTMLDataFromResponse(
					entity, domid);
		} else if (entity.indexOf(domid0) != -1) {
			plOfficialLeftHisRelationHtml = getNormalHTMLDataFromResponse(
					entity, domid0);
		} else if (entity.indexOf(domid1) != -1) {
			plOfficialLeftHisRelationHtml = getNormalHTMLDataFromResponse(
					entity, domid1);
		} else if (entity.indexOf("$CONFIG['product'] = 'enterpriseV2'") != -1) {
			// 这里是一些无法爬取关系的企业用户或机关用户
			return followsStr;
		} else {
			String htmlFileName = "entity" + new Date().getTime() + ".html";
			FileUtils.saveToFile(entity, htmlFileName, "utf-8");
			logger.error("日啊，又有新的关注列表模板!!!!!!!!!解析用户的关注列表出错，页面保存至："
					+ htmlFileName);
			return "0";
		}
		Document document = Jsoup.parse(plOfficialLeftHisRelationHtml,
				"http://weibo.com");
		Elements follows = document.select("li[node-type=user]");
		if (follows != null) {
			follows = document.select("li[action-type=itemClick]");
		}
		for (int i = 0; i < follows.size(); i++) {
			String userId = follows.get(i).attr("usercard");
			if ("".equals(userId)) {
				userId = follows.get(i).attr("data-uid");
			} else {
				userId = userId.substring(3);
			}
			if ("".equals(userId)) {
				userId = follows.get(i).attr("action-data");
				userId = userId.substring(4, userId.indexOf("&"));
			}

			if (userId == null || "".equals(userId)) {
				String htmlFileName = "entity" + new Date().getTime() + ".html";
				FileUtils.saveToFile(entity, htmlFileName, "utf-8");
				logger.error("Pl_Official_LeftHisRelation解析用户的关注列表出错，页面保存至："
						+ htmlFileName);
			}
			followsStr += "," + userId;
		}
		return followsStr;
	}

	/**
	 * 获取微博用户的关注列表和粉丝列表
	 * 
	 * @param weiboUser
	 * @return
	 */

	public WeiboUser getWeiboAllUserInfo(WeiboUser weiboUser) {
		String userId = weiboUser.getUserId();
		int followNum = weiboUser.getFollowNum();
		int fansNum = weiboUser.getFansNum();
		int followPage = followNum / 20 + 1;
		if (followPage > 10) {
			followPage = 10;
		}
		int fansPage = fansNum / 20 + 1;
		if (fansPage > 10) {
			fansPage = 10;
		}
		String followUserIds = "";
		String fansUserIds = "";

		if (followNum > 0) {
			for (int i = 1; i <= followPage; i++) {
				String url = "http://weibo.com/" + userId + "/follow?page=" + i;
				String entity = null;
				entity = Fetcher.getRawHtml(url);
				if (entity == null) {
					break;
				}
				String currentUids = getUserFollowFromRaw(entity);
				if ("0".equals(currentUids)) {
					currentUids = "";
					logger.error("当前出错的URL：" + url);
				}
				followUserIds += currentUids;
			}
			if ("".equals(followUserIds)) {
				followUserIds = null;
			} else {
				followUserIds = followUserIds.substring(1);
			}
		}

		if (fansNum > 0) {
			for (int i = 1; i <= fansPage; i++) {
				String url = "http://weibo.com/" + userId + "/fans?page=" + i;
				String entity = null;
				entity = Fetcher.getRawHtml(url);
				if (entity == null) {
					break;
				}
				String currentUids = getUserFollowFromRaw(entity);
				if ("0".equals(currentUids)) {
					currentUids = "";
					logger.error("当前出错的URL：" + url);
				}
				fansUserIds += currentUids;
			}
			if ("".equals(fansUserIds)) {
				fansUserIds = null;
			} else {
				fansUserIds = fansUserIds.substring(1);
			}
		}
		weiboUser.setFollowUserId(followUserIds);
		weiboUser.setFansUserId(fansUserIds);
		return weiboUser;
	}
}
