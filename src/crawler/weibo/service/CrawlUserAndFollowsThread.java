package crawler.weibo.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import crawler.weibo.dao.UserJdbcService;
import crawler.weibo.model.WeiboUser;
import crawler.weibo.service.parser.SimpleUserParser;
import crawler.weibo.service.parser.UserParser;

public class CrawlUserAndFollowsThread implements Runnable {
	CrawlUserRelations cc;
	UserParser userParser = new UserParser();
	private static final Log logger = LogFactory
			.getLog(CrawlUserAndFollowsThread.class);

	CrawlUserAndFollowsThread(CrawlUserRelations cc) {
		this.cc = cc;
	}

	private int insertUser(WeiboUser weiboUser) {
		return UserJdbcService.getInstance().inSertWeiboUser(weiboUser);
	}

	private int updateUser(WeiboUser weiboUser) {
		return UserJdbcService.getInstance().upDateWeiboUser(weiboUser);
	}

	private WeiboUser getUser(String uid) {
		return UserJdbcService.getInstance().getWeiboUser(uid);
	}

	@Override
	public void run() {
		while (true) {
			long userId;
			userId = cc.getUserIdfromIdArr();
			if (userId == 0) {
				break;
			}
			WeiboUser weiboUser = getUserAllFromClientByUid(
					String.valueOf(userId), true);
			if (weiboUser == null) {
				continue;
			}
			if (weiboUser.getFollowUserId() != null) {
				String[] follows = weiboUser.getFollowUserId().split(",");
				for (int i = 0; i < follows.length; i++) {
					String followUid = follows[i];
					if (!"".equals(followUid)) {
						getUserAllFromClientByUid(followUid, false);
					}
				}
			}
			if (weiboUser.getFansUserId() != null) {
				String[] fans = weiboUser.getFansUserId().split(",");
				for (int i = 0; i < fans.length; i++) {
					String fansUid = fans[i];
					if (!"".equals(fansUid)) {
						getUserAllFromClientByUid(fansUid, false);
					}
				}
			}
		}
		logger.info("已经没有可以获取的userID了，线程即将退出..");
	}

	/**
	 * 根据用户ID爬取该用户的简单信息以及关注和粉丝信息,若该id存在，爬取并存盘
	 * 
	 * @param userId
	 * @return
	 */
	public WeiboUser getUserAllFromClientByUid(String userId, boolean ret) {
		String statusReport = (cc != null) ? "当前进度" + cc.getStatusReport() : "";
		WeiboUser oWeiboUser = null;
		if (ret) {// 需要返回该用户
			oWeiboUser = getUser(String.valueOf(userId));
			if (oWeiboUser != null) {
				return oWeiboUser;
			} else {
				if (!CrawlUserRelations.checkUserIdInUserIdList(userId)) {// 在UserIdList没有找到该用户，自己动手去爬
					WeiboUser weiboUser = SimpleUserParser
							.getSimpleWeiboUserInfo(String.valueOf(userId));
					if (weiboUser == null) {
						return null;
					}
					return updateWeiboUserToOracle(weiboUser, oWeiboUser);
				} else {// 有其他进程正在爬取这个用户，等待爬完，再取出来
					oWeiboUser = getUser(String.valueOf(userId));
					int count = 0;
					while (oWeiboUser == null) {
						if (count > 50) {
							logger.error("有其他进程正在爬取" + userId
									+ "，等待该进程爬完,但是我等得花儿都谢了");
							break;
						}
						logger.info("有其他进程正在爬取用" + userId + "，等待该进程爬完,当前等待次数"
								+ count++);
						try {
							Thread.sleep(15000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						oWeiboUser = getUser(String.valueOf(userId));
					}
					return oWeiboUser;
				}
			}
		} else {// 不需要返回该用户，该用户存在直接返回null
			if (!CrawlUserRelations.checkUserIdInUserIdList(userId)) {// 若UserIdList中无该用户则加入该用户Id,开始爬这个用户
				WeiboUser weiboUser = SimpleUserParser
						.getSimpleWeiboUserInfo(String.valueOf(userId));
				if (weiboUser == null) {
					return null;
				}
				return updateWeiboUserToOracle(weiboUser, null);
			} else {
				logger.info(userId + "已经在库！" + statusReport);
				return null;
			}
		}

	}

	/**
	 * 判断用户是否在库，若不在，直接爬取改用户并插入，若在，判断是否需要更新（变动>4），若需要，爬改用户的粉丝和关注并更新
	 * 
	 * @param weiboUser
	 */
	private WeiboUser updateWeiboUserToOracle(WeiboUser weiboUser,
			WeiboUser oWeiboUser) {
		String statusReport = (cc != null) ? "当前进度" + cc.getStatusReport() : "";
		if (oWeiboUser == null) {
			weiboUser = userParser.getWeiboAllUserInfo(weiboUser);
			if (insertUser(weiboUser) == 1) {
				logger.info(weiboUser.getScreenName() + ":新增入库成功!"
						+ statusReport);
			} else {
				logger.error(weiboUser.getScreenName() + ":新增入库失败!"
						+ statusReport);
			}
		} else {
			int oldSum = oWeiboUser.getMessageNum() + oWeiboUser.getFollowNum();
			int newSum = weiboUser.getMessageNum() + weiboUser.getFollowNum();
			String followIds = oWeiboUser.getFollowUserId();
			if (newSum - 10 > oldSum || followIds == null
					|| "".equals(followIds)) {// 有4个以上的变动
				logger.info(weiboUser.getScreenName() + "：原关注"
						+ oWeiboUser.getFollowNum() + ",原微博"
						+ oWeiboUser.getMessageNum() + ",现关注"
						+ weiboUser.getFollowNum() + ",现微博"
						+ weiboUser.getMessageNum() + ",变动:"
						+ (newSum - oldSum));
				weiboUser = userParser.getWeiboAllUserInfo(weiboUser);
				if (updateUser(weiboUser) == 1) {
					logger.info(weiboUser.getScreenName() + ":更新入库成功!"
							+ statusReport);
				} else {
					logger.error(weiboUser.getScreenName() + ":更新入库失败!"
							+ statusReport);
				}
			} else {
				logger.info(weiboUser.getScreenName() + "不需要更新！" + statusReport);
				return oWeiboUser;
			}
			// 这里以后可以继续加入微博信息爬取方法
		}
		return weiboUser;
	}

	/**
	 * 判断用户是否需要入库,并且不更新关注列表
	 * 
	 * @param weiboUser
	 */
	@SuppressWarnings("unused")
	private void updateWeiboUserToOracleNoFollows(WeiboUser weiboUser) {
		if (weiboUser == null) {
			return;
		}
		WeiboUser oWeiboUser = getUser(weiboUser.getUserId());
		if (oWeiboUser == null) {
			if (insertUser(weiboUser) == 1) {
				logger.info(weiboUser.getScreenName() + ":*新增入库成功!");
			} else {
				logger.error(weiboUser.getScreenName() + ":*新增入库失败!");
			}
		} else {
			int oldSum = oWeiboUser.getMessageNum() + oWeiboUser.getFollowNum();
			int newSum = weiboUser.getMessageNum() + weiboUser.getFollowNum();
			String followIds = oWeiboUser.getFollowUserId();
			if (newSum > oldSum && followIds != null && !"".equals(followIds)) {
				logger.info(weiboUser.getScreenName() + "：原关注数："
						+ oWeiboUser.getFollowNum() + ",原微博数："
						+ oWeiboUser.getMessageNum() + ",现关注数："
						+ weiboUser.getFollowNum() + ",现微博数："
						+ weiboUser.getMessageNum() + ",变动--"
						+ (newSum - oldSum));
				if (updateUser(weiboUser) == 1) {
					logger.info(weiboUser.getScreenName() + ":*更新入库成功!");
				} else {
					logger.error(weiboUser.getScreenName() + ":*更新入库失败!");
				}
			} else {
				weiboUser.setFollowUserId(oWeiboUser.getFollowUserId());
				logger.info(weiboUser.getScreenName() + "*不需要更新！");
			}
			// 这里以后可以继续加入微博信息爬取方法
		}
	}
}
