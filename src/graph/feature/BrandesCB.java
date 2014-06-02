package graph.feature;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;

import utils.FileUtils;
import utils.GraphUtils;

public class BrandesCB {

	public static void main(String[] args) {
		Date d1 = new Date();
		String baseUrl = "G:\\data\\forward\\1936617550\\";
		// String baseUrl = "G:\\data\\forward\\test\\";
		if (args.length > 0) {
			baseUrl = args[0];
		}
		int g[][] = GraphUtils.getAdjTableGraphfromEdgesFileWithCount(baseUrl
				+ "edges.csv");
		calcCB(g, baseUrl);
		System.out.println("运行时间:" + (new Date().getTime() - d1.getTime())
				/ 1000);
		FileUtils.openFile(baseUrl);
	}

	/**
	 * 用Brandes的算法计算图的介数中心性
	 * 
	 * @param g
	 *            二维邻接矩阵
	 * @return 每个节点的介数中心值
	 */
	public static double[] calcCB(int[][] g, String baseUrl) {
		System.out.println("开始运行fastCB");
		int num = g.length;
		System.out.println("当前有" + num + "个节点");
		double CB[] = new double[num];
		for (int s = 0; s < num; s++) {
			LinkedList<Integer> Q = new LinkedList<Integer>();
			LinkedList[] P = new LinkedList[num];
			LinkedList<Integer> S = new LinkedList<Integer>();
			Q.add(s);
			int sigma[] = new int[num];// 最短路径条数
			int dist[] = new int[num];// 最短路径长度
			for (int n = 0; n < num; n++) {
				dist[n] = Integer.MAX_VALUE;
				P[n] = new LinkedList<Integer>();
			}
			sigma[s] = 1;
			dist[s] = 0;
			while (!Q.isEmpty()) {
				int v = Q.poll();
				S.push(v);
				for (int w = 0; w < g[v].length; w++) {
					// w为v的每一个邻点
					if (dist[g[v][w]] > dist[v] + 1) {
						Q.add(g[v][w]);
						dist[g[v][w]] = dist[v] + 1;
					}
					if (dist[g[v][w]] == dist[v] + 1) {
						sigma[g[v][w]] = sigma[g[v][w]] + sigma[v];
						P[g[v][w]].add(v);
					}
				}
			}
			double delta[] = new double[num];

			while (!S.isEmpty()) {
				int w = S.pop();
				while (!P[w].isEmpty()) {
					int v = (Integer) P[w].poll();
					delta[v] = delta[v] + (double) sigma[v] / (double) sigma[w]
							* (1 + delta[w]);
				}
				if (w != s) {
					CB[w] = CB[w] + delta[w];
				}
			}
			System.out.println("已经完成" + s + "/" + num);
		}
		printCBResult(CB, baseUrl);
		return CB;
	}

	/**
	 * 输出结果，最后结果保存在url路径下的CB.csv文件内
	 * 
	 * @param CB
	 * @param url
	 *            路径名称
	 */
	private static void printCBResult(double[] CB, String url) {
		FileWriter fw = null;
		int len = CB.length;
		double normalization = 1 / ((double) (len - 1) * (len - 2));// 归一化
		System.out.println(normalization);
		try {
			fw = FileUtils.getFileWriter(url + "CB.csv", false);
			for (int n = 0; n < CB.length; n++) {
				CB[n] = CB[n] * normalization;
				fw.write(CB[n] + "\r\n");
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
}
