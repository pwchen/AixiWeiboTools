package crawler.weibo.service.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class InitSchedulerService {

	private static final Log logger = LogFactory
			.getLog(InitSchedulerService.class);

	/**
	 * 解析web传来的JSON文本，解析为任务，并加入任务队列
	 * 
	 * @param text
	 */
	public static int feedTaskFromText(String text) {
		JSONArray jsonArr = null;
		int size = 0;
		try {
			jsonArr = new JSONArray(text);
			for (int i = 0; jsonArr != null && i < jsonArr.length(); i++) {
				JSONObject jsonObj = jsonArr.getJSONObject(i);
				Task newTask = new Task(jsonObj.getString("userId"), 0,
						jsonObj.getInt("depth"));
				Scheduler.pushTask(newTask);
				size++;
			}
			logger.info("导入了" + size + "个任务！");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return size;
	}
}
