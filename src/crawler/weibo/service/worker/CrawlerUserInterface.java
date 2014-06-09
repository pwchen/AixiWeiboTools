package crawler.weibo.service.worker;

import crawler.weibo.model.WeiboUser;
import crawler.weibo.service.scheduler.Task;

/**
 * @author Administrator
 * 
 */
public interface CrawlerUserInterface {
	/**
	 * 爬取用户基本信息，以及用户的粉丝和关注信息，如果任务深度为1，则粉丝和关注不再加入任务队列，大于1则将粉丝和关注加入队列
	 */
	public void crawlerUserInfAndReations(Task task);

	/**
	 * 仅仅爬取微博的用户基本信息，不包括关注和粉丝关系，根据这些信息过滤用户
	 * 
	 * @param task
	 */
	public WeiboUser crawlerUserInf(Task task);

	/**
	 * 根据任务中的用户ID，爬取该用户的关注和粉丝关系
	 * 
	 * @param task
	 */
	public WeiboUser crawlerUserReations(WeiboUser wu);

}
