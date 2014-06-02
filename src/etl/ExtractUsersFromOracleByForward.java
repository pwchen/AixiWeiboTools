package etl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import utils.FileUtils;
import crawler.weibo.dao.UserJdbcService;
import crawler.weibo.model.WeiboUser;
import crawler.weibo.service.parser.SimpleUserParser;

public class ExtractUsersFromOracleByForward {
	static String baseFileUrl = "G:\\data\\forward\\";
	private static final Log logger = LogFactory
			.getLog(ExtractUsersFromOracleByForward.class);
	static List<Long> nodeList = new ArrayList<Long>();
	static List<Long> dropUserList = new ArrayList<Long>();
	static int nodeSize = 0;

	public static void main(String[] args) {
		String msgUrl = "2715526174/AerifkSPG";
		msgUrl = msgUrl.replace("/", "\\");
		String fatheFilerUrl = baseFileUrl + msgUrl + "\\";
		extractNodeDataByForwardFile(fatheFilerUrl);
		dropUserList = null;
		FileUtils.openFile(fatheFilerUrl);
	}

	/**
	 * 通过某条微博的转发用户nodes文件，根据深度提取该用户的关系节点
	 * 
	 * @param uId
	 * @param depth
	 */
	public static void extractNodeDataByForwardFile(String fatheFilerUrl) {
		logger.info("当前提取节点方式：转发文件-" + fatheFilerUrl);
		Long[] idList = getUserIdArrFromNodesFile(fatheFilerUrl
				+ "retweetedusers.csv");
		String nodeFileUrl = fatheFilerUrl + "allnodes.csv";
		initNodeFileWriter(nodeFileUrl);
		extractNodesByIdList(idList, nodeFileUrl);
	}

	/**
	 * 从一个nodes文件中获取用户ID列表
	 * 
	 * @param nodeFileUrl
	 * @return
	 */
	private static Long[] getUserIdArrFromNodesFile(String forwardFileUrl) {
		File file = new File(forwardFileUrl);
		FileReader fr = null;
		BufferedReader br = null;
		List<Long> userIdList = new ArrayList<Long>();
		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			String str = br.readLine();
			while ((str = br.readLine()) != null) {
				String userId = str.split(",")[0];
				if (userId != null
						&& !userIdList.contains(Long.parseLong(userId))) {
					userIdList.add(Long.parseLong(userId));
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
				fr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		int listSize = userIdList.size();
		logger.info("从转发文件中获取转发该微博用户数量：" + listSize);
		Long[] a = new Long[listSize];
		return userIdList.toArray(a);
	}

	/**
	 * 将id列表中的用户写入nodes文件存盘，并返回这些用户的粉丝和关注列表，列表中的用户都未存盘
	 * 
	 * @param idList
	 *            用户id列表
	 * @param url
	 *            nodes文件地址
	 * @param ret
	 *            是否需要返回粉丝和关注,false 返回null
	 * @return 粉丝和关注id列表
	 */
	private static void addIdListToNodeFile(Long[] idList, String url,
			boolean ret) {
		int wirtedCount = 0;
		int idListSize = idList.length;
		double win = idListSize > 10 ? idListSize / 10 : 0.1d;
		int percentage = 0;
		FileWriter fw = FileUtils.getFileWriter(url, true);
		try {
			for (long userId : idList) {
				if (!nodeList.contains(userId)
						&& !dropUserList.contains(userId)) {
					WeiboUser wu = UserJdbcService.getInstance().getWeiboUser(
							String.valueOf(userId));
					if (wu == null) {
						logger.info(userId + "未在库，准备用SimpleUserParser爬取");
						wu = SimpleUserParser.getSimpleWeiboUserInfo(String
								.valueOf(userId));
					}
					if (wu != null) {
						int fansNum = wu.getFansNum();
						int followNum = wu.getFollowNum();
						String str = userId + "," + fansNum + "," + followNum
								+ "\r\n";
						fw.write(str);
						nodeList.add(userId);
						wirtedCount++;
						if ((wirtedCount / win) > percentage) {
							logger.info("加载比例：" + (++percentage) + "0%.");
						}
					} else {// 一般进入不来这里，但有一种情况，初始的userIdList中就有没在数据库中的
						dropUserList.add(userId);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtils.closeFileWriter(fw);
		}
		logger.info("当前写入nodes数：" + wirtedCount + "，总nodes数:" + nodeList.size()
				+ "，dropUserList数:" + dropUserList.size());
	}

	/**
	 * 根据url初始nodes.csv文件
	 * 
	 * @param url
	 * @return
	 */
	private static void initNodeFileWriter(String url) {
		FileWriter fw = FileUtils.getFileWriter(url, false);
		try {
			fw.write("id,fans,follows,fansidlist,followidlist\r\n");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtils.closeFileWriter(fw);
		}
	}

	/**
	 * 根据深度抽取用户的关系，并存入文件
	 * 
	 * @param idList
	 * @param depth
	 */
	public static void extractNodesByIdList(Long[] idList, String url) {
		addIdListToNodeFile(idList, url, false);
		nodeSize = nodeList.size();
		logger.info("抽取全部节点完毕！nodeSize:" + nodeSize + ",准备载入节点的边.");
	}
}
