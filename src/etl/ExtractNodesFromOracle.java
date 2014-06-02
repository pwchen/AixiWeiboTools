package etl;

import graph.feature.CalcAllFeature;

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

public class ExtractNodesFromOracle {
	static String baseFileUrl = "G:\\data\\forward\\";
	private static final Log logger = LogFactory
			.getLog(ExtractNodesFromOracle.class);
	static List<Long> nodeList = new ArrayList<Long>();
	static List<Long> dropUserList = new ArrayList<Long>();
	static List<long[]> edgeList = new ArrayList<long[]>();
	private static ArrayList<Long>[] userIdListArr = UserJdbcService
			.getInstance().getUserIdListArray(128);
	static int nodeSize = 0;
	static int edgeSize = 0;

	public static void main(String[] args) {
		if (args.length > 0) {
			if ("uId".equals(args[0])) {
				long uId = Long.parseLong(args[1]);
				extractNodeData(uId, null, true);
			} else {
				String msgUrl = args[1];
				extractNodeData(0, msgUrl, false);
			}
		} else {
			// long uId = 1266321801;
			String msgUrl = "1762325394/AeAwhqd2m";
			extractNodeData(0, msgUrl, false);
		}
	}

	/**
	 * 选择抽取数据的方式， true为uId，false为msgUrl
	 * 
	 * @param uId
	 * @param msgUrl
	 * @param byUid
	 */
	public static void extractNodeData(long uId, String msgUrl, boolean byUid) {
		String fatheFilerUrl = "";
		if (byUid) {
			extractNodeDataByUserId(uId, 3);
			fatheFilerUrl = baseFileUrl + uId + "\\";
		} else {
			msgUrl = msgUrl.replace("/", "\\");
			fatheFilerUrl = baseFileUrl + msgUrl + "\\";
			extractNodeDataByForwardFile(fatheFilerUrl, 2);
		}
		userIdListArr = null;
		dropUserList = null;
		generateEdgesFile(fatheFilerUrl);
		FileUtils.openFile(fatheFilerUrl);
		CalcAllFeature.main(new String[] { fatheFilerUrl });
	}

	/**
	 * 根据nodes文件，生成边文件
	 * 
	 * @param forwardFilefatherUrl
	 */
	private static void generateEdgesFile(String fatheFilerUrl) {
		File file = new File(fatheFilerUrl + "allnodes.csv");
		FileReader fr = null;
		BufferedReader br = null;

		int win = nodeSize / 10;
		int percentage = 0;
		int count = 0;
		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			String str = br.readLine();
			while ((str = br.readLine()) != null) {// 400n(n+m)
				String[] lineArr = str.split(",");
				long userId = Long.parseLong(lineArr[0]);
				String fansIds = lineArr[3];
				String followIds = lineArr[4];
				if (!"null".equals(fansIds)) {
					String[] fansIdArr = fansIds.split(";");
					for (String targetUserId : fansIdArr) {
						long target = Long.parseLong(targetUserId);
						if (nodeList.contains(target)) {//这个是肯定不会重复的
							long[] edge = { userId, target };
							edgeList.add(edge);
						}
					}
				}
				if (!"null".equals(followIds)) {
					String[] followIdArr = followIds.split(";");
					for (String sourceUserId : followIdArr) {
						long source = Long.parseLong(sourceUserId);
						if (checkInNodeList(source)) {
							addToEdgeList(source, userId);
						}
					}
				}
				count++;
				if ((count / win) > percentage) {
					logger.info("当前生成边处理的节点进度" + (++percentage) + "0%.");
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
		edgeSize = edgeList.size();
		logger.info("边初始化完毕，当前的nodeSize:" + nodeSize + ",edgeSize:" + edgeSize
				+ ",准备开始清理无入度节点");
		cleanNoIndegreeNodes();
		writeNodesListToFile(fatheFilerUrl + "nodes.csv");
		writeEdgesListToFile(fatheFilerUrl + "edges.csv");
	}

	/**
	 * 清理部分无入度的节点
	 */
	private static void cleanNoIndegreeNodes() {
		nodeList.clear();
		for (long[] edge : edgeList) {
			if (!nodeList.contains(edge[1])) {
				nodeList.add(edge[1]);
			}
		}
		nodeSize = nodeList.size();
		logger.info("将nodeList的节点清理，找到有入度的节点:" + nodeSize);
		List<long[]> clearEdgeList = new ArrayList<long[]>();// 多一点存储空间，但速度会快很多
		for (int i = 0; i < edgeSize; i++) {
			long[] edge = edgeList.get(i);
			if (nodeList.contains(edge[0])) {
				clearEdgeList.add(edge);
			}
		}
		edgeList.clear();
		edgeList = clearEdgeList;
		edgeSize = edgeList.size();
		logger.info("清理无入度节点完毕,准备写入节点文件和边文件,剩余边:" + edgeSize);
	}

	/**
	 * 将 节点索引List写入文件nodes.csv
	 */
	private static void writeNodesListToFile(String url) {
		FileWriter fw = FileUtils.getFileWriter(url, false);
		try {
			for (long uId : nodeList) {
				fw.write(uId + "\r\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtils.closeFileWriter(fw);
		}
	}

	/**
	 * 将边List写入文件edges.csv
	 */
	private static void writeEdgesListToFile(String url) {
		FileWriter fw = initEdgeFileWriter(url);
		try {
			for (long[] edge : edgeList) {
				int source = getNodeIndex(edge[0]);
				int target = getNodeIndex(edge[1]);
				fw.write(source + "," + target + "\r\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtils.closeFileWriter(fw);
		}
	}

	/**
	 * 获取节点的索引
	 * 
	 * @param uId
	 * @return
	 */
	private static int getNodeIndex(long uId) {
		return nodeList.indexOf(uId);
	}

	/**
	 * 检测边是否edgeList中，若不在，加入该边
	 * 
	 * @param userId
	 * @param targetUserId
	 */
	private static void addToEdgeList(long source, long target) {
		for (long[] temp : edgeList) {
			if (temp[0] == source && temp[1] == target) {
				return;
			}
		}
		long[] edge = { source, target };
		edgeList.add(edge);
	}

	/**
	 * 通过某个用户ID，根据深度提取该用户的关系节点
	 * 
	 * @param uId
	 * @param depth
	 */
	public static void extractNodeDataByUserId(long uId, int depth) {
		logger.info("当前提取节点方式：当个用户-" + uId);
		String nodeFileUrl = baseFileUrl + uId + "\\allnodes.csv";
		initNodeFileWriter(nodeFileUrl);
		Long[] idList = new Long[] { uId };
		extractNodesByIdList(idList, depth, nodeFileUrl);
	}

	/**
	 * 通过某条微博的转发用户nodes文件，根据深度提取该用户的关系节点
	 * 
	 * @param uId
	 * @param depth
	 */
	public static void extractNodeDataByForwardFile(String fatheFilerUrl,
			int depth) {
		logger.info("当前提取节点方式：转发文件-" + fatheFilerUrl);
		Long[] idList = getUserIdArrFromNodesFile(fatheFilerUrl
				+ "retweetedusers.csv");
		String nodeFileUrl = fatheFilerUrl + "allnodes.csv";
		initNodeFileWriter(nodeFileUrl);
		extractNodesByIdList(idList, depth, nodeFileUrl);
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
				if (userId != null) {
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
	private static Long[] addIdListToNodeFile(Long[] idList, String url,
			boolean ret) {
		List<Long> userIdList = new ArrayList<Long>();
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
					if (wu != null) {
						int fansNum = wu.getFansNum();
						int followNum = wu.getFollowNum();
						String fansIds = wu.getFansUserId();
						fansIds = (fansIds == null) || "".equals(fansIds) ? "null"
								: fansIds.replace(",", ";");
						String followsIds = wu.getFollowUserId();
						followsIds = (followsIds == null)
								|| "".equals(followsIds) ? "null" : followsIds
								.replace(",", ";");
						String str = userId + "," + fansNum + "," + followNum
								+ "," + fansIds + "," + followsIds + "\r\n";
						fw.write(str);
						nodeList.add(userId);
						wirtedCount++;
						if ((wirtedCount / win) > percentage) {
							logger.info("加载比例：" + (++percentage) + "0%.");
						}
						if (ret) {
							String[] fansArr = (wu.getFansUserId() == null) ? null
									: (wu.getFansUserId().split(","));
							// 检查这些用户，若不在nodes文件中，且在数据库中就加入到需要返回的idList中
							for (int i = 0; fansArr != null
									&& i < fansArr.length; i++) {
								String uId = fansArr[i];
								if (uId != null && !"".equals(uId)) {
									long uIdL = Long.parseLong(uId);
									if (checkInUserIdList(uIdL))
										userIdList.add(uIdL);
								}
							}
							String[] followsArr = (wu.getFollowUserId() == null) ? null
									: (wu.getFollowUserId().split(","));
							for (int i = 0; followsArr != null
									&& i < followsArr.length; i++) {
								String uId = followsArr[i];
								if (uId != null && !"".equals(uId)) {
									long uIdL = Long.parseLong(uId);
									if (checkInUserIdList(uIdL))
										userIdList.add(uIdL);
								}
							}
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
		if (ret) {
			int listSize = userIdList.size();
			logger.info("当前写入nodes数：" + wirtedCount + "，总nodes数:"
					+ nodeList.size() + ";返回数：" + listSize);
			Long[] a = new Long[listSize];
			return userIdList.toArray(a);
		}
		logger.info("当前写入nodes数：" + wirtedCount + "，总nodes数:" + nodeList.size()
				+ "，dropUserList数:" + dropUserList.size());
		return null;
	}

	/**
	 * 检测是否在UserIdList中，根据索引
	 * 
	 * @param userId
	 * @return
	 */
	private static boolean checkInUserIdList(long userId) {
		return userIdListArr[(int) (userId % 128)].contains(userId);
	}

	/**
	 * 检测用户ID是否在nodeList中
	 * 
	 * @param userId
	 * @return
	 */
	private static boolean checkInNodeList(long userId) {
		if (nodeList.contains(userId)) {
			return true;
		}
		return false;
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
	 * 根据url初始nodes.csv文件
	 * 
	 * @param url
	 * @return
	 */
	private static FileWriter initEdgeFileWriter(String url) {
		logger.info("写入边文件:" + url);
		FileWriter fw = FileUtils.getFileWriter(url, false);
		try {
			edgeSize = edgeList.size();
			fw.write(nodeSize + "," + edgeSize + "\r\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fw;
	}

	/**
	 * 根据深度抽取用户的关系，并存入文件
	 * 
	 * @param idList
	 * @param depth
	 */
	public static void extractNodesByIdList(Long[] idList, int depth, String url) {
		logger.info("当前处理depth:" + depth);
		if (--depth == 0) {
			addIdListToNodeFile(idList, url, false);
			nodeSize = nodeList.size();
			logger.info("抽取全部节点完毕！nodeSize:" + nodeSize + ",准备载入节点的边.");
		} else {
			Long[] idArr = addIdListToNodeFile(idList, url, true);
			extractNodesByIdList(idArr, depth, url);
		}
	}
}
