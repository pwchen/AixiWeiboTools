package crawler.weibo.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.edu.gdut.weibo.model.WeiboUser;

public class CrawUserConnectionFromOneUser {
	private static final Log logger = LogFactory
			.getLog(CrawUserConnectionFromOneUser.class);

	public static void main(String[] args) {
		String firstUid = "1197161814";// "1197161814：李开复";"1266321801:姚晨""1936617550自己"
		Long[] uIdArr = crawlerSigleUser(firstUid);
		logger.info("获取该用户粉丝及关注用户数量：" + uIdArr.length);
		new CrawlUserConnection(uIdArr, uIdArr.length).getUserConnection();
		// ExtractNodesFromOracle.main(new String[] { "uId", firstUid });
	}

	/**
	 * 爬取单个用户基本信息，并返回其关注和粉丝ID列表
	 * 
	 * @param firstUid
	 * @return
	 */
	private static Long[] crawlerSigleUser(String firstUid) {
		logger.info("爬取入口ID：" + firstUid);
		WeiboUser weiboUser = new CrawlUserAndFollowsThread(null)
				.getUserAllFromClientByUid(firstUid, true);
		if (weiboUser == null) {
			return null;
		}
		List<Long> userList = new ArrayList<Long>();
		String userListStr = weiboUser.getFollowUserId();
		if (userListStr != null && !"".equals(userListStr)) {
			String[] followUserArr = userListStr.split(",");
			for (String temp : followUserArr) {
				if (temp != null && !"".equals(temp)) {
					addIdtoUserIdList(userList, temp);
				}
			}
		}
		userListStr = weiboUser.getFansUserId();
		if (userListStr != null && !"".equals(userListStr)) {
			String[] fansUserArr = userListStr.split(",");
			for (String temp : fansUserArr) {
				if (temp != null && !"".equals(temp)) {
					addIdtoUserIdList(userList, temp);
				}
			}
		}
		Long[] a = new Long[userList.size()];
		return userList.toArray(a);
	}

	/**
	 * 将id保存到UserIdList里面，如果id已存在，则不保存
	 * 
	 * @param userIdList
	 * @param string
	 */
	private static void addIdtoUserIdList(List<Long> userIdList, String idStr) {
		if (idStr != null && !"".equals(idStr)) {
			long uid = Long.parseLong(idStr);
			if (!userIdList.contains(uid)) {
				userIdList.add(uid);
			}
		}
	}
}
