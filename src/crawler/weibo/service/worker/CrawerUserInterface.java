package crawler.weibo.service.worker;

/**
 * @author Administrator
 * 
 */
public interface CrawerUserInterface {
	/**
	 * 爬取用户基本信息，以及用户的粉丝和关注信息，粉丝和关注不再加入任务队列
	 */
	public void crawerUserInfAndReations(String userId);

	/**
	 * 爬取用户基本信息，以及用户的粉丝和关注信息，并将粉丝和关注加入任务队列
	 */
	public void crawerUserInfAndReationsAndTask(String userId);
}
