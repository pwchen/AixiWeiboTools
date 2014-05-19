package crawler.weibo.service.worker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;

import utils.CrawlerContext;
import crawler.weibo.model.WeiboUser;
import crawler.weibo.service.filter.WeiboUserFilter;
import crawler.weibo.service.login.WeiboLoginHttpClientUtils;
import crawler.weibo.service.scheduler.Scheduler;
import crawler.weibo.service.scheduler.Task;

/**
 * 工作线程
 * 
 * @author Administrator
 * 
 */
public class Worker implements Runnable, CrawlerUserInterface {
	private Task initTask = null;
	private HttpClient httpClient = null;
	private static final Log logger = LogFactory.getLog(Worker.class);

	public Worker(Task task) {
		this.initTask = task;
	}

	@Override
	public void run() {
		httpClient = WeiboLoginHttpClientUtils.getLoginhttpClient();
		if (initTask != null) {
			doWorkBytask(initTask);
		}
		initTask = Scheduler.pollTask();
		int sleepCount = CrawlerContext.getContext().getFailureNumber();// 等待10*20s依然没有任务，将退出该线程
		while (true) {
			if (initTask == null) {
				try {
					logger.info("队列中暂时没有任务，休息20s.");
					Thread.sleep(20000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if ((sleepCount--) < 0)
					break;
			} else {
				doWorkBytask(initTask);
				Scheduler.removeWorkingTask(initTask);
			}
			initTask = Scheduler.pollTask();
		}
	}

	/**
	 * 将来用于选择爬用户或者爬取类型，选择暂时用户爬
	 * 
	 * @param task
	 */
	private void doWorkBytask(Task task) {
		if (task.getType() == 0) {
			crawlerUserInfAndReations(task);
		} else {
			// 其他爬取类型
		}
	}

	@Override
	public void crawlerUserInfAndReations(Task task) {
		WeiboUser wu = crawlerUserInf(task);
		if (!WeiboUserFilter.filterUserByRules(wu)) {
			wu = crawlerUserReations(task, wu);
		}
		if (task.getDepth() > 1) {
			assignSuccessorTask(task, wu);
		}
	}

	/**
	 * 指定后续的任务并存入任务队列中
	 * 
	 * @param task
	 * @param wu
	 */
	private void assignSuccessorTask(Task task, WeiboUser wu) {
		int depth = task.getDepth() - 1;
		String[] relations = wu.generateRelationArray();
		for (String uId : relations) {
			Task newTask = new Task(uId, 0, depth);
			Scheduler.pushTask(newTask);
		}

	}

	@Override
	public WeiboUser crawlerUserInf(Task task) {
		String userId = task.getUserId();
		return null;
	}

	@Override
	public WeiboUser crawlerUserReations(Task task, WeiboUser wu) {
		return null;
	}

}
