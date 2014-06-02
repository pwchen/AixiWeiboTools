package graph.feature;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;

import utils.FileUtils;
import utils.GraphUtils;

public class FastCC {
	static double maxCC = 0;
	static int maxcount[];
	static double totalOutDegree = 0;

	public static void main(String[] args) {
		try {
			Thread.sleep(600000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Date d1 = new Date();
		// String baseUrl = "G:\\data\\forward\\test\\";
		String baseUrl = "G:\\data\\forward\\1936617550\\";
		if (args.length > 0) {
			baseUrl = args[0];
		}
		int g[][] = GraphUtils.getAdjTableGraphfromEdgesFileWithCount(baseUrl
				+ "edges.csv");
		calcOutDegree(g, baseUrl);
		calcCC(g, baseUrl);
		System.out.println("运行时间:" + (new Date().getTime() - d1.getTime())
				/ 1000);
		FileUtils.openFile(baseUrl);
	}

	/**
	 * 计算出度，并保存至文件
	 * 
	 * @param g
	 * @param baseUrl
	 */
	private static void calcOutDegree(int[][] g, String baseUrl) {
		int len = g.length;
		FileWriter fw = null;
		try {
			fw = FileUtils.getFileWriter(baseUrl + "OutDegree.csv", false);
			for (int n = 0; n < len; n++) {
				totalOutDegree += (double) g[n].length;
				fw.write(g[n].length + "\r\n");
			}
			FileUtils.writeNewFileOnce(baseUrl + "AverageOutDegree.csv",
					totalOutDegree / len + "");
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
	 * 用Brandes的算法计算图的Closeness Centrality,顺带着吧平均最短路径，网络直径，平均出度也计算了一遍
	 * 
	 * @param g
	 *            二维数组的图
	 * @return
	 */
	public static double[] calcCC(int[][] g, String baseUrl) {
		System.out.println("开始计算Closeness Centrality");
		int num = g.length;
		maxcount = new int[num];
		System.out.println("当前有" + num + "个节点");
		double CC[] = new double[num];
		for (int s = 0; s < num; s++) {
			LinkedList<Integer> Q = new LinkedList<Integer>();
			Q.add(s);
			int dist[] = new int[num];// 最短路径长度
			for (int n = 0; n < num; n++) {
				dist[n] = Integer.MAX_VALUE;
			}
			dist[s] = 0;
			while (!Q.isEmpty()) {
				int v = Q.poll();
				for (int w = 0; w < g[v].length; w++) {
					// w为v的每一个邻点
					if (dist[g[v][w]] > dist[v] + 1) {
						Q.add(g[v][w]);
						dist[g[v][w]] = dist[v] + 1;
						if (maxCC < dist[g[v][w]]) {
							maxCC = dist[g[v][w]];
						}
					}
				}
			}
			for (int n = 0; n < num; n++) {
				if (dist[n] < Integer.MAX_VALUE) {
					CC[s] = CC[s] + dist[n];
				} else {
					maxcount[s]++;
				}
			}
			System.out.println("已经完成" + (s + 1) + "/" + num);
		}
		double totalDist = 0;
		double totalDistWithUnReach = 0;
		int hasnopathcount = 0;
		System.out.println("最大的最短路径长度:" + maxCC);
		for (int n = 0; n < num; n++) {// 那些不可到达的节点的距离为（最大的最短路径）
			totalDist += CC[n];
			hasnopathcount += maxcount[n];
			CC[n] = CC[n] + (maxcount[n] * maxCC);
			totalDistWithUnReach += CC[n];
		}
		System.out.println("全部路径长度(不包含不可到达节点)：" + totalDist);
		System.out.println("全部路径长度(不可到达节点路径长度为最大路径长度)：" + totalDistWithUnReach);
		double avergeShortestPathLength = totalDist
				/ ((double) num * (num - 1) - hasnopathcount);
		double avergeShortestPathLengthWithUnReach = totalDistWithUnReach
				/ ((double) num * (num - 1));
		System.out.println("平均路径长度(不包含不可到达节点)：" + avergeShortestPathLength);
		System.out.println("平均路径长度(包含不可到达节点)："
				+ avergeShortestPathLengthWithUnReach);
		FileUtils.writeNewFileOnce(baseUrl + "AvergeShortestPathLength.csv",
				avergeShortestPathLength + ",");
		FileUtils.appendWriteOnce(baseUrl + "AvergeShortestPathLength.csv",
				avergeShortestPathLengthWithUnReach + ",");
		printCCResult(CC, baseUrl);
		return CC;
	}

	/**
	 * 输出结果，最后结果保存在url路径下的CB.csv文件内
	 * 
	 * @param CB
	 * @param url
	 *            路径名称
	 */
	private static void printCCResult(double[] CC, String url) {
		double len = (double) CC.length;
		FileWriter fw = null;
		double normalization = (double) (len - 1);// 归一化
		try {

			fw = FileUtils.getFileWriter(url + "CC.csv", false);
			for (int n = 0; n < len; n++) {
				CC[n] = normalization / CC[n];
				fw.write(CC[n] + "\r\n");
			}

			FileUtils.writeNewFileOnce(url + "NetworkDiameter.csv", maxCC + "");
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
}
