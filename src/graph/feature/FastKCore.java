package graph.feature;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import utils.FileUtils;
import utils.GraphUtils;

public class FastKCore {

	public static void main(String[] args) {
		Date d1 = new Date();
		// String baseUrl = "G:\\data\\forward\\test\\";
		String baseUrl = "G:\\data\\forward\\1936617550\\";
		if (args.length > 0) {
			baseUrl = args[0];
		}
		int g[][] = GraphUtils.getAdjTableGraphfromEdgesFileWithCount(baseUrl
				+ "edges.csv");
		calcKC(g, baseUrl);
		System.out.println("运行时间:" + (new Date().getTime() - d1.getTime())
				/ 1000);
		FileUtils.openFile(baseUrl);
	}

	/**
	 * 这里g只能是邻接表，邻接矩阵没那么大内存
	 * 
	 * @param g
	 * @return
	 */
	public static int[] calcKC(int[][] g, String baseUrl) {
		System.out.println("开始计算K-Core");
		int num = g.length;// 总节点数
		System.out.println("找到节点" + num);
		int[] KC = new int[num];
		ArrayList<Integer> deletedNodes = new ArrayList<Integer>();

		int[] degreeNum = new int[num];// 度数量
		int minDegree = 1;// 当前处理的最小度数
		System.out.println("初始化degreeNum。");
		// 初始化degreeNum
		for (int i = 0; i < num; i++) {
			for (int j = 0; j < g[i].length; j++) {
				degreeNum[i]++;
				degreeNum[g[i][j]]++;
			}
		}
		printAllDegree(degreeNum, baseUrl);
		System.out.println("初始化degreeNum完毕。");
		while (deletedNodes.size() < num) {
			System.out.println("开始删除度为" + minDegree + "的节点。");
			boolean flag = true;// 标兵
			while (flag) {
				ArrayList<Integer> newDeletedNodes = new ArrayList<Integer>();
				flag = false;
				for (int i = 0; i < num; i++) {
					if (degreeNum[i] <= minDegree) {
						if (deletedNodes.contains(i)) {
							continue;
						}
						KC[i] = minDegree;// 该节点的K-Core
						flag = true;
						newDeletedNodes.add(i);
					}
				}
				if (flag) {
					for (int i = 0; i < num; i++) {
						if (newDeletedNodes.contains(i)) {
							for (int j = 0; j < g[i].length; j++) {
								degreeNum[g[i][j]]--;
							}
						} else {
							for (int j = 0; j < g[i].length; j++) {
								if (newDeletedNodes.contains(g[i][j])) {
									degreeNum[i]--;
								}
							}
						}

					}
				} else {// 扫描没有变化，进入下一个k
					minDegree++;
				}
				for (int i = 0; i < newDeletedNodes.size(); i++) {
					deletedNodes.add(newDeletedNodes.get(i));
				}
				System.out.print(".");// 迭代一次
			}
			System.out.println("处理进程" + deletedNodes.size() + "/" + num + "。");
		}
		printKCResult(KC, baseUrl);
		return KC;
	}

	/**
	 * 将节点的度保存至文件（出度+入度）
	 * 
	 * @param degreeNum
	 */
	private static void printAllDegree(int[] degreeNum, String baseUrl) {
		FileWriter fw = null;
		long totalDegree = 0;
		try {
			fw = FileUtils.getFileWriter(baseUrl + "Degree.csv", false);
			for (int n = 0; n < degreeNum.length; n++) {
				totalDegree += degreeNum[n];
				fw.write(degreeNum[n] + "\r\n");
			}
			double aved = (double) totalDegree / degreeNum.length;
			FileUtils
					.writeNewFileOnce(baseUrl + "AverageDegree.csv", aved + "");
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
	 * 将节点k-CORE值保存文件，的度保存至文件（出度+入度）
	 * 
	 * @param KC
	 * @param url
	 */
	private static void printKCResult(int[] KC, String url) {
		FileWriter fw = null;
		try {
			fw = FileUtils.getFileWriter(url + "KC.csv", false);

			for (int n = 0; n < KC.length; n++) {
				fw.write(KC[n] + "\r\n");
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
