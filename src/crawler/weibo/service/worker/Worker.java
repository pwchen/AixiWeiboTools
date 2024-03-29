package crawler.weibo.service.worker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import utils.CrawlerContext;
import crawler.weibo.dao.UserJdbcService;
import crawler.weibo.model.WeiboLoginedClient;
import crawler.weibo.model.WeiboUser;
import crawler.weibo.service.fetcher.Fetcher;
import crawler.weibo.service.filter.WeiboUserFilter;
import crawler.weibo.service.login.WeiboLoginHttpClientUtils;
import crawler.weibo.service.parser.UserParser;
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
	WeiboLoginedClient wlClient = null;
	private static final Log logger = LogFactory.getLog(Worker.class);

	public Worker(Task task) {
		this.initTask = task;
	}

	public Worker() {
	}

	@Override
	public void run() {
		wlClient = WeiboLoginHttpClientUtils.getWeiboLoginedClient();
		if (initTask != null) {
			doWorkBytask(initTask);
		}
		initTask = Scheduler.pollTask();
		int sleepCount = CrawlerContext.getContext().getFailureNumber();// 等待10*20s依然没有任务，将退出该线程
		while (true) {
			if (initTask == null) {
				try {
					logger.info("队列中任务数量为" + Scheduler.getScheduleList().size()
							+ "，休息10s.");
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if ((sleepCount--) < -100)
					break;
			} else {
				doWorkBytask(initTask);
				Scheduler.removeWorkingTask(initTask);
			}
			initTask = Scheduler.pollTask();
		}
		logger.warn("线程退出...");
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
			logger.error("获得一个错误的任务类型:" + task);
			// 其他爬取类型
		}
	}

	@Override
	public void crawlerUserInfAndReations(Task task) {
		logger.info("开始任务：" + task);
		WeiboUser wu = UserJdbcService.getInstance().getWeiboUser(
				task.getUserId());
		if (wu != null) {// 数据库中已经存在该用户，则直接指派后续任务
			logger.info("数据库中已经存在该用户:" + wu.getScreenName() + "，任务深度为"
					+ task.getDepth());
			if (task.getDepth() > 1) {
				assignSuccessorTask(task, wu);
			}
			return;
		}
		wu = crawlerUserInf(task);
		if (wu != null && !WeiboUserFilter.filterUserByRules(wu)) {
			wu = crawlerUserReations(wu);
			int result = UserJdbcService.getInstance().inSertWeiboUser(wu);
			if (result > 0) {
				logger.info("用户：" + wu.getScreenName() + "加入数据库！");
				if (task.getDepth() > 1) {
					assignSuccessorTask(task, wu);
				}
			} else { // 其他未知原因
				logger.error("用户：" + wu.getScreenName() + "存入数据库失败！");
			}
		} else if (wu == null) {
			logger.warn(task + "爬取失败！");
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
		if (relations == null)
			return;
		int addCount = 0;
		for (String uId : relations) {
			addCount++;
			Task newTask = new Task(uId, 0, depth);
			Scheduler.pushTask(newTask);
		}
		logger.info("为用户：" + wu.getScreenName() + "指定了" + addCount + "个后续任务.");
	}

	@Override
	public WeiboUser crawlerUserInf(Task task) {
		String userId = task.getUserId();
		logger.info("开始采集用户ID：" + userId + "的基本信息...");
		String rawHtml = Fetcher.fetchUserInfoHtmlByUid(userId, wlClient);
		if (rawHtml == null) {
			return null;
		}
		WeiboUser wu = UserParser.paserUserInfo(rawHtml);
		if (wu == null)
			return null;
		wu.setUserId(userId);
		return wu;
	}

	@Override
	public WeiboUser crawlerUserReations(WeiboUser wu) {

		String userId = wu.getUserId();
		int followNum = wu.getFollowNum();
		int fansNum = wu.getFansNum();
		logger.info("开始采集用户：" + wu.getUserId() + "-" + wu.getScreenName()
				+ " 的粉丝" + fansNum + "及关注" + followNum);
		int followPage = followNum / 20 + 1;
		if (followPage > 10) {
			followPage = 10;
		}
		int fansPage = fansNum / 20 + 1;
		if (fansPage > 10) {
			fansPage = 10;
		}
		String followUserIds = "";
		String fansUserIds = "";

		if (followNum > 0) {
			for (int i = 1; i <= followPage; i++) {
				String entity = Fetcher.fetchUserFollows(userId, i, wlClient);
				if (entity == null) {
					break;
				}
				String currentUids = UserParser.paserUserRelations(entity);
				if ("0".equals(currentUids)) {
					currentUids = "";
					logger.error("当前出错的id：" + userId + "/follow?page=" + i);
					i--;
					continue;
				}
				followUserIds += currentUids;
			}
			if ("".equals(followUserIds)) {
				followUserIds = null;
			} else {
				followUserIds = followUserIds.substring(1);
			}
		}

		if (fansNum > 0) {
			for (int i = 1; i <= fansPage; i++) {
				String entity = Fetcher.fetchUserFans(userId, i, wlClient);
				if (entity == null) {
					break;
				}
				String currentUids = UserParser.paserUserRelations(entity);
				if ("0".equals(currentUids)) {
					currentUids = "";
					logger.warn("当前出错的id：" + userId + "/fans?page=" + i);
					i--;
					continue;
				}
				fansUserIds += currentUids;
			}
			if ("".equals(fansUserIds)) {
				fansUserIds = null;
			} else {
				fansUserIds = fansUserIds.substring(1);
			}
		}
		wu.setFollowUserId(followUserIds);
		wu.setFansUserId(fansUserIds);
		return wu;
	}
}
