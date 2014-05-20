package crawler.weibo.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import utils.CrawlerContext;
import crawler.weibo.service.scheduler.Scheduler;
import crawler.weibo.service.scheduler.Task;
import crawler.weibo.service.worker.Worker;

public class ThreadPool {

	public static void main(String[] agrs) {
		int threadNumber = CrawlerContext.getContext().getThreadNumber();
		ExecutorService executorService = Executors
				.newFixedThreadPool(threadNumber);
		Task task = new Task("1786783961", 0, 3);
		Scheduler.pushTask(task);
		for (int i = 0; i < threadNumber; i++) {
			Worker thread = new Worker();
			executorService.execute(thread);
		}
	}
}
