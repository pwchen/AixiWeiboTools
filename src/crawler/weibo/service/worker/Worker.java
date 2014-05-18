package crawler.weibo.service.worker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;

import utils.CrawlerContext;
import crawler.weibo.service.login.WeiboLoginHttpClientUtils;
import crawler.weibo.service.scheduler.Scheduler;
import crawler.weibo.service.scheduler.Task;

/**
 * 工作线程
 * 
 * @author Administrator
 * 
 */
public class Worker implements Runnable, CrawerUserInterface {
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

	private void doWorkBytask(Task task) {
		if (task.getType() == 0) {
			crawerUserInfAndReations(task.getUserId());
		} else if (task.getType() == 1) {
			crawerUserInfAndReationsAndTask(task.getUserId());
		}
	}

	@Override
	public void crawerUserInfAndReations(String userId) {

	}

	@Override
	public void crawerUserInfAndReationsAndTask(String userId) {

	}

}
