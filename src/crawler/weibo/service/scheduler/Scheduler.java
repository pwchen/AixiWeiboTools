package crawler.weibo.service.scheduler;

import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import crawler.weibo.service.filter.WeiboUserFilter;

/**
 * 工作任务队列
 * 
 * @author Administrator
 * 
 */
public class Scheduler {
	/**
	 * 待完成工作队列
	 */
	private static LinkedList<Task> scheduleList = new LinkedList<Task>();
	/**
	 * 正在进行的工作队列
	 */
	private static LinkedList<Task> workingList = new LinkedList<Task>();
	private static final Log logger = LogFactory.getLog(Scheduler.class);

	/**
	 * 从工作队列中获取一个工作,并将该工作放入正在进行的工作队列
	 * 
	 * @return
	 */
	public static synchronized Task pollTask() {
		if (scheduleList.size() < 1) {
			return null;
		}
		Task task = (Task) getScheduleList().pollFirst();
		workingList.push(task);
		// logger.info("从队列中取出" + task + ",当前剩余工作:" + scheduleList.size()
		//		+ "，正在工作数量:" + workingList.size());
		return task;
	}

	/**
	 * 完成该任务后将该task从正在进行的工作队列中移除
	 * 
	 * @param task
	 * @return
	 */
	public static synchronized Task removeWorkingTask(Task task) {
		workingList.remove(task);
		//logger.info("正在进行的工作队列移除" + task);
		return task;

	}

	/**
	 * 检查改task是否在队列中
	 * 
	 * @param task
	 * @return
	 */
	public static synchronized boolean checkTask(Task task) {
		if (Scheduler.getWorkingList().contains(task)) {// 工作队列
			logger.info(task+"已经正在工作！");
			return true;
		}
		if (Scheduler.getScheduleList().contains(task)) {// 任务队列
			logger.info(task+"已经存在工作队列中！");
			return true;
		}
		if (WeiboUserFilter.filterUserByFilUserTab(task.getUserId())) {// 用户过滤表
			return true;
		}
		if (task.getDepth() <= 1
				&& WeiboUserFilter.filterUserByUserTab(task.getUserId()))// 用户表
		{
			return true;
		}
		return false;
	}

	/**
	 * 将一个任务加入工作队列中
	 * 
	 * @param task
	 */
	public static synchronized int pushTask(Task task) {
		if (checkTask(task))
			return 0;
		Scheduler.getScheduleList().add(task);
		// logger.info(task.toString() + "→加入工作队列,当前剩余工作:" + scheduleList.size()
		//		+ "，正在工作数量:" + workingList.size());
		return 1;
	}

	public static LinkedList<Task> getScheduleList() {
		return scheduleList;
	}

	public static void setScheduleList(LinkedList<Task> sList) {
		Scheduler.scheduleList = sList;
	}

	public static LinkedList<Task> getWorkingList() {
		return workingList;
	}

	public static void setWorkingList(LinkedList<Task> workingList) {
		Scheduler.workingList = workingList;
	}

	public static Log getLogger() {
		return logger;
	}

}
