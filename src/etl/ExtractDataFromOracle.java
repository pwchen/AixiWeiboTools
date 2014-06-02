package etl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import crawler.weibo.dao.UserJdbcService;
import crawler.weibo.model.WeiboUser;
import crawler.weibo.service.parser.SimpleUserParser;

public class ExtractDataFromOracle {
	private static final Log logger = LogFactory
			.getLog(ExtractDataFromOracle.class);
	static List<WeiboUser> userList = new ArrayList<WeiboUser>();
	static List<String> leafUserList = new ArrayList<String>();// 第三层用户
	static String fileUrl = "G:\\data\\forward\\3200285974\\AcNAaxRYO\\";
	static String edgesFileName = fileUrl + "edges.csv";
	static String nodesFileName = fileUrl + "nodes.csv";
	static String retweetedFile = fileUrl + "retweetedusers.csv";
	static int edgeCount = 0;
	static int failure = 0;
	static int[][] edges;
	static int[] nodes;

	public static void main(String args[]) {
		extractDataByMsgRetweetTrace(retweetedFile);
		CleanNoIndegreeNodes.clearEdges(fileUrl);
	}

	/**
	 * 初始化文件，将原有的文件备份
	 */
	private static void initFiles() {
		logger.info("初始化文件...");
		File file = new File(edgesFileName);
		if (file.exists()) {
			File bakFile = new File(edgesFileName + ".bak");
			if (bakFile.exists()) {
				bakFile.delete();
			}
			file.renameTo(new File(edgesFileName + ".bak"));
		}
		file = new File(nodesFileName);
		if (file.exists()) {
			File bakFile = new File(nodesFileName + ".bak");
			if (bakFile.exists()) {
				bakFile.delete();
			}
			file.renameTo(new File(nodesFileName + ".bak"));
		}
	}

	/**
	 * 从一个转发路径用户列表中获取数据并生成图文件:节点文件和边文件
	 * 
	 * @param fileUrl
	 */
	public static void extractDataByMsgRetweetTrace(String fileUrl) {
		initFiles();
		File file = new File(fileUrl);
		FileReader fr = null;
		LineNumberReader lnr = null;
		try {
			fr = new FileReader(file);
			lnr = new LineNumberReader(fr);
			lnr.readLine();
			String s = null;
			while ((s = lnr.readLine()) != null) {
				String userId = s.split(",")[0];
				if (userId != null && !"".equals(userId)) {
					WeiboUser wu = UserJdbcService.getInstance().getWeiboUser(
							userId);
					if (wu == null) {
						logger.error("userId:" + userId + ",在数据库中未找到。");
						continue;
					}
					int index = saveToNodesArrList(wu);
					if (index != -1) {// 新加入
						saveToEdgesFile(index, wu);
					}
				}
			}
			saveToNodesFile(userList, nodesFileName);
			logger.info("第三层用户有：" + leafUserList.size());
			connectedLeafUser();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				lnr.close();
				fr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 将第三层与网络相关的边加入
	 */
	private static void connectedLeafUser() {
		for (int i = 0; i < leafUserList.size(); i++) {
			String uId = leafUserList.get(i);
			WeiboUser wu = UserJdbcService.getInstance().getWeiboUser(uId);
			if (wu == null) {
				logger.error(uId + "在数据库中未找到!");
				continue;
			}

			int index = getUserIndexNoCrawler(uId);
			if (index == -1) {
				logger.error(uId + "在添加索引!");
				continue;
			}
			String followsStr = wu.getFollowUserId();
			if (followsStr != null && !"".equals(followsStr)) {
				String[] followArr = followsStr.split(",");
				for (String userId : followArr) {
					if (userId != null && !"".equals(userId)) {
						int origin = getUserIndexNoCrawler(userId);
						if (origin != -1) {
							addEdgeToFile(origin, index);
						}
					}
				}
			}
			String fansStr = wu.getFansUserId();
			if (fansStr != null && !"".equals(fansStr)) {
				String[] fansArr = fansStr.split(",");
				for (String userId : fansArr) {
					if (userId != null && !"".equals(userId)) {
						int target = getUserIndexNoCrawler(userId);
						if (target != -1) {
							addEdgeToFile(index, target);
						}
					}
				}
			}

		}
	}

	/**
	 * 将userList里的用户信息写入节点文件内
	 */
	private static void saveToNodesFile(List<WeiboUser> list, String fileName) {
		FileWriter fw = null;
		int size = list.size();
		try {
			fw = new FileWriter(fileName);
			fw.write("id,uid,follows,fans\r\n");
			for (int i = 0; i < size; i++) {
				WeiboUser wu = list.get(i);
				fw.write(i + "," + wu.getUserId() + "," + wu.getFollowNum()
						+ "," + wu.getFansNum() + "\r\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		logger.info("共获取节点:" + list.size() + ",保存节点文件至:" + fileName);
	}

	/**
	 * 将改用户的所有关注者加入表文件中
	 * 
	 * @param index
	 * @param wu
	 */
	private static void saveToEdgesFile(int index, WeiboUser wu) {
		String followsStr = wu.getFollowUserId();
		if (followsStr != null && !"".equals(followsStr)) {
			String[] followArr = followsStr.split(",");
			for (String userId : followArr) {
				if (userId != null && !"".equals(userId)) {
					int origin = getUserIndex(userId);
					if (origin != -1) {
						addToListNoRepeat(leafUserList, userId);
						addEdgeToFile(origin, index);
					}
				}
			}
		}
		String fansStr = wu.getFansUserId();
		if (fansStr != null && !"".equals(fansStr)) {
			String[] fansArr = fansStr.split(",");
			for (String userId : fansArr) {
				if (userId != null && !"".equals(userId)) {
					int target = getUserIndex(userId);
					if (target != -1) {
						addToListNoRepeat(leafUserList, userId);
						addEdgeToFile(index, target);
					}
				}
			}
		}
	}

	/**
	 * 加入不重复的uid到list
	 * 
	 * @param list
	 * @param userId
	 */
	private static void addToListNoRepeat(List<String> list, String userId) {
		if (!list.contains(userId)) {
			list.add(userId);
		}
	}

	/**
	 * 根据节点索引加入一条边,注意调换边的方向为关注用户指向被关注用户
	 * 
	 * @param originIndex
	 * @param targetIndex
	 */
	private static void addEdgeToFile(int originIndex, int targetIndex) {
		if (!checkEdge(originIndex, targetIndex)) {
			try {
				// 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
				FileWriter writer = new FileWriter(edgesFileName, true);
				writer.write(originIndex + "," + targetIndex + "\r\n");
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			edgeCount++;
		}
		// System.out.println(originIndex + "→" + targetIndex);
	}

	/**
	 * 检查该边是否在文件当中，若存在该边返回true，不存在则返回false
	 * 
	 * @param originIndex
	 * @param targetIndex
	 * @return
	 */
	private static boolean checkEdge(int originIndex, int targetIndex) {
		File file = new File(edgesFileName);
		if (!file.exists()) {
			return false;
		}
		FileReader fr = null;
		LineNumberReader lnr = null;
		try {
			fr = new FileReader(file);
			lnr = new LineNumberReader(fr);
			String str = null;
			while ((str = lnr.readLine()) != null) {
				if (str.equals(originIndex + "," + targetIndex)) {
					return true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				lnr.close();
				fr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * 获取用户的节点索引,若改节点不再userList中返回-1
	 * 
	 * @param userId
	 * @return
	 */
	private static int getUserIndexNoCrawler(String userId) {
		int size = userList.size();
		for (int i = 0; i < size; i++) {
			if (userId.equals(userList.get(i).getUserId())) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 获取用户的节点索引,若没有，将会尝试爬取该用户基本信息，若爬取失败返回-1
	 * 
	 * @param userId
	 * @return
	 */
	private static int getUserIndex(String userId) {
		int size = userList.size();
		for (int i = 0; i < size; i++) {
			if (userId.equals(userList.get(i).getUserId())) {
				return i;
			}
		}
		WeiboUser wu = UserJdbcService.getInstance().getWeiboUser(userId);
		if (wu == null) {
			wu = getSimpleUserFromCrawler(userId);
			if (wu != null) {
				logger.info(userId + "在数据库中未找到.临时爬取:" + wu.getScreenName()
						+ ":" + wu.getFollowNum());
				UserJdbcService.getInstance().inSertWeiboUser(wu);
			} else {
				logger.warn(userId + "临时爬取改用户失败");
				failure++;
				return -1;
			}
		}
		saveToNodesArrList(wu);
		return size;
	}

	private static WeiboUser getSimpleUserFromCrawler(String userId) {
		return SimpleUserParser.getSimpleWeiboUserInfo(userId);
	}

	/**
	 * 将用户保存到节点userList中:若该用户已经存在，则返回-1，若不存在，保存该用户并返回索引
	 * 
	 * @param wu
	 * @return
	 */
	private static int saveToNodesArrList(WeiboUser wu) {
		int size = userList.size();
		for (int i = 0; i < size; i++) {
			if (wu.getUserId().equals(userList.get(i).getUserId())) {
				return -1;
			}
		}
		userList.add(wu);
		return size;
	}
}
