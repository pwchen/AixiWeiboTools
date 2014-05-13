package crawler.weibo.test;

import java.io.UnsupportedEncodingException;
import java.util.List;

import utils.MsgJdbcService;
import utils.UserJdbcService;
import crawler.weibo.model.WeiboUser;

public class TE {
	/**
	 * @param args
	 * @throws UnsupportedEncodingException
	 */
	public static void main(String[] args) throws UnsupportedEncodingException {
		List<String> userIds = UserJdbcService.getInstance()
				.getUserIDfromUser();
		for (int j = 0; j < userIds.size(); j++) {
			String userId ="1784501333"; //userIds.get(j);
			WeiboUser weiboUser = UserJdbcService.getInstance().getWeiboUser(
					userId);
			// String followUserIds = weiboUser.getFollowUserId();
			String[] followsArr = clear(weiboUser).split(",");
			System.out.println("[" + j + "/[" + userIds.size()
					+ "]]>>>" + "开始搜索" + userId + "的关注用户" + followsArr.length
					+ "转发数....");
			for (int i = 0; i < followsArr.length; i++) {
				if (i > 0 && followsArr[i].equals(followsArr[i - 1])) {
					continue;
				}
				String followUserId = followsArr[i];
				int retweetNum = 0;
				WeiboUser followUser = UserJdbcService.getInstance()
						.getWeiboUser(followUserId);
				if (followUser == null) {
					// System.out.println(followUserId + "用户没有找到");
					continue;
				}
				// int totalNum = followUser.getMessageNum();
				retweetNum = MsgJdbcService.getInstance().getMsgsNumbyUid(
						userId, followUserId);
				if (retweetNum >= 40) {
					System.out.println(userId + ">>>" + followUserId + ">>>"
							+ retweetNum);
				}
			}
		}
	}

	/**
	 * 清除微博用户的关注用户id列表中重复的id
	 * 
	 * @param weiboUser
	 * @return
	 */
	private static String clear(WeiboUser weiboUser) {
		String[] followsArr = weiboUser.getFollowUserId().split(",");
		String followsStr = "";
		int flag = 0;
		for (int i = 0; i < followsArr.length; i++) {
			if (followsStr.indexOf(followsArr[i]) == -1) {
				followsStr += followsArr[i] + ",";
			} else {
				flag++;
			}
		}
		if (followsStr.length() > 1) {
			followsStr.substring(0, followsStr.length() - 1);
		}
		if (flag > 1) {
			System.out.println(flag);
			weiboUser.setFollowUserId(followsStr);
			UserJdbcService.getInstance().upDateWeiboUser(weiboUser);
			System.out.println(weiboUser.getScreenName() + "更新了");
		}
		return followsStr;
	}
}
