package crawler.weibo.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import utils.CrawlerContext;
import utils.SystemPropertiesUtils;
import crawler.weibo.service.scheduler.Scheduler;
import crawler.weibo.service.scheduler.Task;
import crawler.weibo.service.worker.Worker;

public class CrawleWeiboUserService {

	private static final Log logger = LogFactory
			.getLog(CrawleWeiboUserService.class);
	private static ExecutorService executorService = null;
	private static ArrayList<Worker> threadList = new ArrayList<Worker>();

	/**
	 * 初始化配置文件包括操作界面的用户列表，还有读取的爬取入口文件
	 */
	public static void initConfiguration() {
		// getUserTaskByText("");
		// getUserTaskByFile("config\task.aixi");
	}

	/**
	 * 爬微博入口
	 * 
	 * @param args
	 */
	public static void startCrawle() {
		logger.info("start crawling!");
		int threadNumber = CrawlerContext.getContext().getThreadNumber();
		executorService = Executors.newFixedThreadPool(threadNumber);
		// if (Scheduler.pollTask() == null) {
		// logger.error("任务队列中未发现入口任务，程序退出...");
		// }
		for (int i = 0; i < threadNumber; i++) {
			Worker thread = new Worker();
			threadList.add(thread);
			executorService.execute(thread);
		}
	}

	/**
	 * 暂停爬虫
	 * 
	 * @return
	 */
	public static String pauseCrawler() {
		if (executorService == null || threadList.size() != 0) {
			logger.info("当前没有线程~!");
			return "null";
		} else {
			logger.info("当前有线程~" + threadList.size());
			for (int i = 0; i < threadList.size(); i++) {
				try {
					threadList.get(i).wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			logger.info("暂停所有的Worker");
			return "success";
		}
	}

	/**
	 * 继续爬虫
	 * 
	 * @return
	 */
	public static String resumeCrawler() {
		if (executorService == null || threadList.size() != 0) {
			startCrawle();
			return "null";
		} else {
			Thread.currentThread().notifyAll();
			logger.info("继续爬取...");
			return "success";
		}
	}

	/**
	 * 根据文本内容，读取一个任务，并存入任务列表 格式为 userid,depth
	 * 
	 * @param text
	 */
	public static void getUserTaskByText(String text) {
		String[] stra = text.split(SystemPropertiesUtils.getLineSeparator());
		for (int i = 0; i < stra.length; i++) {
			String[] taskStr = stra[i].split(",");
			if (taskStr.length != 2) {
				logger.error("任务文本解析失败!改文本内容为：" + stra[i]);
			}
			Task newTask = new Task(taskStr[0], 0, Integer.parseInt(taskStr[1]));
			Scheduler.pushTask(newTask);
		}
	}

	/**
	 * 根据文件名称，读取文件中的文本内容，并存入任务列表 文件文本内容每行的格式为：userid,depth,直接支持CSV格式
	 * 
	 * @param fileName
	 */
	public static void getUserTaskByFile(String fileName) {
		logger.info("读取任务文件" + fileName);
		File file = new File(fileName);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			while ((tempString = reader.readLine()) != null) {
				String[] taskStr = tempString.split(",");
				if (taskStr.length != 2) {
					logger.error("任务文本解析失败!改文本内容为：" + tempString);
				}
				Task newTask = new Task(taskStr[0], 0,
						Integer.parseInt(taskStr[1]));
				Scheduler.pushTask(newTask);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
	}

}
