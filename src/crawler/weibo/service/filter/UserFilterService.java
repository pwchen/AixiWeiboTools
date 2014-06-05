package crawler.weibo.service.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;

import crawler.weibo.dao.UserJdbcService;

public class UserFilterService {

	private static final Log logger = LogFactory
			.getLog(UserFilterService.class);

	/**
	 * 解析web传来的JSON文本，解析为任务，并加入任务队列
	 * 
	 * @param text
	 */
	public static int feedFilterRuleFromText(String text) {
		JSONArray jsonArr = null;
		int size = 0;
		try {
			jsonArr = new JSONArray(text);

			size = WeiboUserFilter.reflashUfrList(jsonArr);
			logger.info("导入了" + size + "个过滤规则！");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return size;
	}

	/**
	 * 加入到过滤表中
	 * 
	 * @param userId
	 * @return
	 */
	public static boolean addToFilterList(String userId, String type) {
		return WeiboUserFilter.addToFilterList(userId, type);
	}
}
