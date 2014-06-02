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

public class CleanNoIndegreeNodes {

	static String fileUrl = null;
	static String edgesFileName = null;
	static String nodesFileName = null;
	static String edgesFileName2 = null;
	static String nodesFileName2 = null;
	static String graphAdjectTableFile = null;

	static List<long[]> userList = new ArrayList<long[]>();
	static List<Integer> userMappingList = new ArrayList<Integer>();
	static int[][] graph;
	static int[] edgeSize;// 每个节点的出度边数量
	static int oldSize;// 原节点数量
	static int newSize;// 除去无入度节点后节点数量
	static int[] edgeEdge;// 边边数组0 为source 1为target
	static int[] noIndegree;

	private static final Log logger = LogFactory
			.getLog(CleanNoIndegreeNodes.class);

	/**
	 * 初始化所需文件
	 */
	public static void initFile(String url) {
		if (url != null) {
			File file = new File(url);
			if (file.exists()) {
				File bakFile = new File(url + ".bak");
				if (bakFile.exists()) {
					bakFile.delete();
				}
				file.renameTo(new File(url + ".bak"));
			}
			return;
		}
		File file = new File(edgesFileName2);
		if (file.exists()) {
			File bakFile = new File(edgesFileName2 + ".bak");
			if (bakFile.exists()) {
				bakFile.delete();
			}
			file.renameTo(new File(edgesFileName2 + ".bak"));
		}
		file = new File(nodesFileName2);
		if (file.exists()) {
			File bakFile = new File(nodesFileName2 + ".bak");
			if (bakFile.exists()) {
				bakFile.delete();
			}
			file.renameTo(new File(nodesFileName2 + ".bak"));
		}

		file = new File(graphAdjectTableFile);
		if (file.exists()) {
			File bakFile = new File(graphAdjectTableFile + ".bak");
			if (bakFile.exists()) {
				bakFile.delete();
			}
			file.renameTo(new File(graphAdjectTableFile + ".bak"));
		}
		logger.info("初始化文件...");
	}

	/**
	 * 清除无发到达的节点,(无入度)
	 */
	public static void clearEdges(String url) {
		fileUrl = url;
		edgesFileName = fileUrl + "edges.csv";
		nodesFileName = fileUrl + "nodes.csv";
		edgesFileName2 = fileUrl + "edges2.csv";
		nodesFileName2 = fileUrl + "nodes2.csv";
		graphAdjectTableFile = fileUrl + "adjectTable.txt";
		initFile(null);
		loadNodes();
		loadGraph();
		saveToNodesFile();
		writeGraphToFile();
	}

	/**
	 * 将图文件存盘（邻接表）
	 */
	private static void writeGraphToFile() {
		initFile(graphAdjectTableFile);
		File file = new File(graphAdjectTableFile);
		FileWriter fw = null;
		try {
			fw = new FileWriter(file);
			for (int i = 0; i < newSize; i++) {
				String edgeStr = "";
				for (int j = 0; j < graph[i].length; j++) {
					edgeStr += graph[i][j] + ",";
				}
				if (!"".equals(edgeStr)) {
					edgeStr = edgeStr.substring(0, edgeStr.length() - 1);
				}
				fw.write(edgeStr + "\r\n");
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
	}

	/**
	 * 保存节点至节点文件
	 */
	private static void saveToNodesFile() {
		for (int i = 0; i < oldSize; i++) {
			if (noIndegree[i] == 1 && userMappingList.indexOf(i) == -1)
				System.out.println(i);
		}
		File file = new File(nodesFileName2);
		FileWriter fw = null;
		try {
			fw = new FileWriter(file);
			fw.write("id,uid,follows,fans\r\n");
			for (int i = 0; i < newSize; i++) {
				int index = userMappingList.get(i);
				long[] temp = userList.get(index);
				String tmstr = i + "," + temp[0] + "," + temp[1] + ","
						+ temp[2] + "\r\n";
				fw.write(tmstr);
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
	}

	/**
	 * 将节点文件载入
	 * 
	 * @param nodesFileName22
	 * @param uList
	 */
	private static void loadNodes() {
		File file = new File(nodesFileName);
		FileReader fr = null;
		LineNumberReader lnr = null;
		try {
			fr = new FileReader(file);
			lnr = new LineNumberReader(fr);
			String s = lnr.readLine();// 跳过第一行
			while ((s = lnr.readLine()) != null) {
				long[] temp = new long[3];
				String[] tempStrs = s.split(",");// id,uid,follows,fans

				temp[0] = Long.parseLong(tempStrs[1]);
				temp[1] = Long.parseLong(tempStrs[2]);
				temp[2] = Long.parseLong(tempStrs[3]);
				userList.add(Integer.parseInt(tempStrs[0]), temp);
			}
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
		oldSize = userList.size();
		logger.info("载入原节点个数:" + oldSize);
	}

	/**
	 * 扫描2此边文件从初始边文件中读取边到二维邻接表，扫描2次边文件，第 一次扫描获取原边的数量以及可到达的边，第二次扫描获取所有边并新节点的出度
	 * 
	 * @return
	 */
	private static void loadGraph() {
		noIndegree = new int[oldSize];// 0 无入度的节点 1 有入度的节点
		File file = new File(edgesFileName);
		FileReader fr = null;
		LineNumberReader lnr = null;
		int[] outDegree = null;// 每个节点边的数量
		int edgeNum = 0;
		try {
			fr = new FileReader(file);
			lnr = new LineNumberReader(fr);
			String s = null;
			while ((s = lnr.readLine()) != null) {
				String[] ss = s.split(",");
				int target = Integer.parseInt(ss[1]);
				noIndegree[target] = 1;// 可到达的节点
				edgeNum++;
			}
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
		for (int i = 0; i < oldSize; i++) {
			if (noIndegree[i] == 1) {
				newSize++;
			}
		}
		logger.info("原始边数：" + edgeNum);
		logger.info("有入度节点个数：" + newSize);

		graph = new int[newSize][];
		outDegree = new int[newSize];
		edgeEdge = new int[edgeNum + edgeNum];
		int n = 0;
		try {
			fr = new FileReader(file);
			lnr = new LineNumberReader(fr);
			String s = null;
			while ((s = lnr.readLine()) != null) {
				String[] ss = s.split(",");
				int source = Integer.parseInt(ss[0]);
				int target = Integer.parseInt(ss[1]);
				if (noIndegree[source] == 1 && noIndegree[target] == 1) {
					outDegree[getIndex(source)]++;
					edgeEdge[n++] = getIndex(source);
					edgeEdge[n++] = getIndex(target);
				}
			}
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
		logger.info("新边数:" + n / 2);
		logger.info("已经映射的节点数量:" + userMappingList.size());
		for (int i = 0; i < newSize; i++) {
			graph[i] = new int[outDegree[i]];
			outDegree[i] = 0;
		}
		file = new File(edgesFileName2);
		FileWriter fw = null;
		int count = 0;
		try {
			fw = new FileWriter(file);
			for (int i = 0; i < n; i++) {
				if (i < (i ^ 1)) {
					graph[edgeEdge[i]][outDegree[edgeEdge[i]]++] = edgeEdge[i ^ 1];
					fw.write(edgeEdge[i] + "," + edgeEdge[i ^ 1] + "\r\n");
					count++;
				}
			}
			logger.info("写入新边：" + count);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 获取新旧映射的index
	 * 
	 * @param i
	 * @return
	 */
	private static int getIndex(Integer i) {
		int newUserListSize = userMappingList.size();
		int index = userMappingList.indexOf(i);
		if (index != -1) {
			return index;
		} else {
			userMappingList.add(i);
			return newUserListSize;
		}
	}
}
