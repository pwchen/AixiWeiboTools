package graph.feature;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import utils.FileUtils;
import crawler.weibo.model.Node;

public class CalcAllFeature {

	private static final Log logger = LogFactory.getLog(CalcAllFeature.class);

	public static void main(String[] args) {
		String baseUrl = "G:\\data\\forward\\1762325394\\AeAwhqd2m\\";
		if (args.length > 0) {
			baseUrl = args[0];
		}
		BrandesCB.main(new String[] { baseUrl });
		FastCC.main(new String[] { baseUrl });
		FastKCore.main(new String[] { baseUrl });
		ClusteringCoefficient.main(new String[] { baseUrl });
		conbineAllFeatrues(baseUrl);
	}

	private static void conbineAllFeatrues(String baseUrl) {
		Long ids[] = loadIdsNodesFile(baseUrl);
		int size = ids.length;
		Node[] nodes = new Node[size];
		for (int i = 0; i < size; i++) {// 载入userID
			nodes[i] = new Node(i);
			nodes[i].setUserId(ids[i]);
		}
		loadFansNumAndFollowNum(nodes, baseUrl);
		loadDegree(nodes, baseUrl);
		loadOutDegree(nodes, baseUrl);
		loadBetweennessCentrality(nodes, baseUrl);
		loadClosenessCentrality(nodes, baseUrl);
		loadKCore(nodes, baseUrl);
		saveNodesToNodesFile(nodes, baseUrl);
	}

	private static void saveNodesToNodesFile(Node[] nodes, String baseUrl) {
		FileWriter fw = FileUtils.getFileWriter(baseUrl + "nodesfeatures.csv",
				false);
		int size = nodes.length;
		try {
			fw.write("userid,fans,follow,degree,outdegree,cb,cc,kc\r\n");
			for (int i = 0; i < size; i++) {
				fw.write(nodes[i].getNodeWriteString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtils.closeFileWriter(fw);
		}
		FileUtils.openFile(baseUrl);
	}

	/**
	 * 从KC.csv文件中载入节点的K-Core核数
	 * 
	 * @param nodes
	 * @param baseUrl
	 */
	private static void loadKCore(Node[] nodes, String baseUrl) {
		logger.info("从KC.csv文件中载入节点的K-Core核数");
		FileReader fr = null;
		BufferedReader br = null;
		int size = nodes.length;
		try {
			fr = new FileReader(baseUrl + "KC.csv");
			br = new BufferedReader(fr);
			String s = null;
			int count = 0;
			int percent = 0;
			while ((s = br.readLine()) != null) {
				nodes[count++].setKc(Integer.parseInt(s));
				if (count / (size / 10) > percent) {
					percent++;
					logger.info("载入进度:" + percent + "0%");
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtils.closeBufferedReader(br);
			FileUtils.closeFileReader(fr);
		}
	}

	/**
	 * 从CC.csv文件中载入节点的ClosenessCentrality
	 * 
	 * @param nodes
	 * @param baseUrl
	 */
	private static void loadClosenessCentrality(Node[] nodes, String baseUrl) {
		logger.info("从CC.csv文件中载入节点的ClosenessCentrality");
		FileReader fr = null;
		BufferedReader br = null;
		int size = nodes.length;
		try {
			fr = new FileReader(baseUrl + "CC.csv");
			br = new BufferedReader(fr);
			String s = null;
			int count = 0;
			int percent = 0;
			while ((s = br.readLine()) != null) {
				nodes[count++].setCc(Double.parseDouble(s));
				if (count / (size / 10) > percent) {
					percent++;
					logger.info("载入进度:" + percent + "0%");
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtils.closeBufferedReader(br);
			FileUtils.closeFileReader(fr);
		}
	}

	/**
	 * 从CB.csv文件中载入节点的BetweennessCentrality
	 * 
	 * @param nodes
	 * @param baseUrl
	 */
	private static void loadBetweennessCentrality(Node[] nodes, String baseUrl) {
		logger.info("从CB.csv文件中载入节点的BetweennessCentrality");
		FileReader fr = null;
		BufferedReader br = null;
		int size = nodes.length;
		try {
			fr = new FileReader(baseUrl + "CB.csv");
			br = new BufferedReader(fr);
			String s = null;
			int count = 0;
			int percent = 0;
			while ((s = br.readLine()) != null) {
				nodes[count++].setBc(Double.parseDouble(s));
				if (count / (size / 10) > percent) {
					percent++;
					logger.info("载入进度:" + percent + "0%");
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtils.closeBufferedReader(br);
			FileUtils.closeFileReader(fr);
		}
	}

	/**
	 * 从OutDegree.csv文件中载入节点的出度
	 * 
	 * @param nodes
	 * @param baseUrl
	 */
	private static void loadOutDegree(Node[] nodes, String baseUrl) {
		logger.info("从OutDegree.csv文件中载入节点的出度");
		FileReader fr = null;
		BufferedReader br = null;
		int size = nodes.length;
		try {
			fr = new FileReader(baseUrl + "OutDegree.csv");
			br = new BufferedReader(fr);
			String s = null;
			int count = 0;
			int percent = 0;
			while ((s = br.readLine()) != null) {
				nodes[count++].setOutDegree(Integer.parseInt(s));
				if (count / (size / 10) > percent) {
					percent++;
					logger.info("载入进度:" + percent + "0%");
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtils.closeBufferedReader(br);
			FileUtils.closeFileReader(fr);
		}
	}

	/**
	 * 从Degree.csv文件中载入节点的度
	 * 
	 * @param nodes
	 * @param baseUrl
	 */
	private static void loadDegree(Node[] nodes, String baseUrl) {
		logger.info("从Degree.csv文件中载入节点的度");
		FileReader fr = null;
		BufferedReader br = null;
		int size = nodes.length;
		try {
			fr = new FileReader(baseUrl + "Degree.csv");
			br = new BufferedReader(fr);
			String s = null;
			int count = 0;
			int percent = 0;
			while ((s = br.readLine()) != null) {
				nodes[count++].setDegree(Integer.parseInt(s));
				if (count / (size / 10) > percent) {
					percent++;
					logger.info("载入进度:" + percent + "0%");
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtils.closeBufferedReader(br);
			FileUtils.closeFileReader(fr);
		}

	}

	/**
	 * 从allnodes.csv文件中载入用户粉丝数量和关注数量
	 * 
	 * @param nodes
	 * @param baseUrl
	 */
	private static void loadFansNumAndFollowNum(Node[] nodes, String baseUrl) {
		logger.info("从allnodes.csv文件中载入用户粉丝数量和关注数量");
		FileReader fr = null;
		BufferedReader br = null;
		int size = nodes.length;
		try {
			fr = new FileReader(baseUrl + "allnodes.csv");
			br = new BufferedReader(fr);
			String s = br.readLine();
			int count = 1;
			int percent = 0;
			while ((s = br.readLine()) != null) {
				String[] ss = s.split(",");
				int id = nodesContains(nodes, ss[0]);
				if (id != -1) {
					int fans = Integer.parseInt(ss[1]);
					int follow = Integer.parseInt(ss[2]);
					nodes[id].setFans(fans);
					nodes[id].setFollow(follow);
					if (count / (size / 10) > percent) {
						percent++;
						logger.info("载入进度:" + percent + "0%");
					}
					if (count++ == size) {
						return;
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtils.closeBufferedReader(br);
			FileUtils.closeFileReader(fr);
		}
	}

	/**
	 * 判断节点列表中是否存在该userId的节点，若有返回改列表索引，若无，返回-1
	 * 
	 * @param nodes
	 * @param userId
	 * @return
	 */
	private static int nodesContains(Node[] nodes, String userId) {
		for (Node node : nodes) {
			long userIdL = Long.parseLong(userId);
			if (userIdL == node.getUserId()) {
				return node.getId();
			}
		}
		return -1;
	}

	/**
	 * 从nodes.csv文件中载入用户的userId
	 * 
	 * @param baseUrl
	 *            Long[userId]
	 * @return
	 */
	private static Long[] loadIdsNodesFile(String baseUrl) {
		logger.info(" 从nodes.csv文件中载入用户的userId");
		FileReader fr = null;
		BufferedReader br = null;
		List<Long> idList = new ArrayList<Long>();
		try {
			fr = new FileReader(baseUrl + "nodes.csv");
			br = new BufferedReader(fr);
			String s = null;
			while ((s = br.readLine()) != null) {
				idList.add(Long.valueOf(s));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtils.closeBufferedReader(br);
			FileUtils.closeFileReader(fr);
		}
		Long[] a = new Long[idList.size()];
		return idList.toArray(a);
	}
}
