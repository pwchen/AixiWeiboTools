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
	private static LinkedList<Task> scheduleList;
	/**
	 * 正在进行的工作队列
	 */
	private static LinkedList<Task> workingList;
	private static final Log logger = LogFactory.getLog(Scheduler.class);

	/**
	 * 从工作队列中获取一个工作,并将该工作放入正在进行的工作队列
	 * 
	 * @return
	 */
	public static synchronized Task pollTask() {
		Task task = (Task) getScheduleList().pollFirst();
		workingList.push(task);
		logger.info("从队列中取出用户ID" + task.getUserId() + "，工作类型" + task.getType()
				+ ",当前剩余工作:" + scheduleList.size());
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
		logger.info("正在进行的工作队列移除" + task);
		return task;

	}

	/**
	 * 检查改task是否在队列中
	 * 
	 * @param task
	 * @return
	 */
	public static synchronized boolean checkTask(Task task) {
		if (Scheduler.getScheduleList().contains(task)) {
			return true;
		}
		if (Scheduler.getWorkingList().contains(task)) {
			return true;
		}
		if (WeiboUserFilter.filterUserByFilUserTab(task.getUserId())) {
			return true;
		}
		return false;
	}

	/**
	 * 将一个任务加入工作队列中
	 * 
	 * @param task
	 */
	public static synchronized void pushTask(Task task) {
		if (checkTask(task))
			return;
		Scheduler.getScheduleList().add(task);
		logger.info("用户ID" + task.getUserId() + "，工作类型" + task.getType()
				+ "加入工作队列,当前剩余工作:" + scheduleList.size());
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
