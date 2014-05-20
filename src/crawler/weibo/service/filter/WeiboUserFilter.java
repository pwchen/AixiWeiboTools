package crawler.weibo.service.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import crawler.weibo.dao.UserJdbcService;
import crawler.weibo.model.WeiboUser;

public class WeiboUserFilter {

	private static final Log logger = LogFactory.getLog(WeiboUserFilter.class);

	/**
	 * 根据规则，判断改用户是否继续爬下去，如果用户被过滤，返回true。
	 * 
	 * @param wu
	 * @return
	 */
	public static boolean filterUserByRules(WeiboUser wu) {
		return false;
	}

	/**
	 * 根据存在数据库中的过滤用户表，如果用户被过滤，返回true。
	 * 
	 * @param wu
	 * @return
	 */
	public static boolean filterUserByFilUserTab(String userId) {
		if (UserJdbcService.getInstance().checkUserFilterList(userId)) {
			logger.info(userId + "已经在过滤表中存在..");
			return true;
		}
		return false;
	}
	
	/**
	 * 根据存在数据库中的用户表，如果用户存在，返回true。
	 * 
	 * @param wu
	 * @return
	 */
	public static boolean filterUserByUserTab(String userId) {
		if ((UserJdbcService.getInstance().getWeiboUser(userId)) != null) {
			logger.info(userId + "已经在用户表中存在..");
			return true;
		}
		return false;
	}

}
