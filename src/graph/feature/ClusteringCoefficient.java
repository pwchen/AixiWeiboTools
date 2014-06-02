package graph.feature;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import utils.FileUtils;
import utils.GraphUtils;

public class ClusteringCoefficient {

	public static void main(String[] args) {
		Date d1 = new Date();
		// String baseUrl = "G:\\data\\forward\\test\\";
		String baseUrl = "G:\\data\\D-SIR model data\\1762325394\\AeAwhqd2m\\";
		if (args.length > 0) {
			baseUrl = args[0];
		}
		int g[][] = GraphUtils.getAdjTableGraphfromEdgesFileWithCount(baseUrl
				+ "edges.csv");
		int h[][] = GraphUtils
				.getOutAdjTableGraphfromEdgesFileWithCount(baseUrl
						+ "edges.csv");
		calcClusteringCoefficient(g, h, baseUrl);
		System.out.println("运行时间:" + (new Date().getTime() - d1.getTime())
				/ 1000);
		FileUtils.openFile(baseUrl);
	}

	private static void calcClusteringCoefficient(int[][] g, int[][] h,
			String baseUrl) {
		int len = g.length;
		double cc[] = new double[len];
		for (int i = 0; i < len; i++) {
			List<Integer> edges = new ArrayList<Integer>();
			for (int j = 0; j < g[i].length; j++) {
				edges.add(g[i][j]);
			}
			for (int j = 0; j < h[i].length; j++) {
				edges.add(h[i][j]);
			}
			for (int node : edges) {
				for (int j = 0; j < g[node].length; j++) {
					if (edges.contains(g[node][j])) {
						cc[i]++;
					}
				}
			}
			int degree = edges.size();
			if (degree == 1) {// 当只有一个节点与之相连的时候，即其所有连接的节点都孤立，因此CC为0
				cc[i] = 0;
			} else {
				cc[i] = cc[i] / (degree * (degree - 1));
			}
			System.out.println("当前节点的聚类系数：" + cc[i] + ",进度:" + (i + 1) + "/"
					+ len);
		}
		double totalCc = 0;
		for (double temp : cc) {
			totalCc += temp;
		}
		totalCc = totalCc / len;
		System.out.println(totalCc);
		FileUtils.writeNewFileOnce(baseUrl + "ClusteringCoefficient111.csv",
				totalCc + "");
	}
}
