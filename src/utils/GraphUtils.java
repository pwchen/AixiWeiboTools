package utils;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import crawler.weibo.model.Node;

public class GraphUtils {

	private static final Log logger = LogFactory.getLog(GraphUtils.class);

	/**
	 * 将以邻接表的文件输入到二维数组中，即转换为邻接矩阵，第一行没有节点数量
	 * 
	 * @param fileUrl
	 *            文件URL
	 * @return 二维邻接矩阵
	 */
	public static int[][] getAdjMatrixfromAdjTableFile(String fileUrl) {
		int n = getLineNum(fileUrl);
		int[][] g = new int[n][n];
		FileReader in = null;
		LineNumberReader reader = null;
		int m = 0;
		try {
			in = new FileReader(fileUrl);
			reader = new LineNumberReader(in);
			String s = reader.readLine();
			while (s != null) {
				String[] strs = s.split(",");
				for (String temp : strs) {
					if (!"".equals(temp)) {
						g[m][Integer.parseInt(temp)] = 1;
					}
				}
				s = reader.readLine();
				m++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return g;
	}

	/**
	 * 从一个边文件中，加载图为的邻接表形式，第一行为节点数量和边数量
	 * 
	 * @param fileUrl
	 * @return
	 */
	public static int[][] getAdjTableGraphfromEdgesFileWithCount(String fileUrl) {
		int n = 0;
		int[][] g = null;
		FileReader in = null;
		LineNumberReader reader = null;
		int m = 0;
		int count = 0;
		int[] origin_list = null;
		int[] target_list = null;
		int[] degree = null;
		try {
			in = new FileReader(fileUrl);
			reader = new LineNumberReader(in);
			String s = reader.readLine();
			String[] strs = s.split(",");
			if (!"".equals(strs[0])) {
				n = Integer.parseInt(strs[0]);
				m = Integer.parseInt(strs[1]);
			}
			g = new int[n][];
			origin_list = new int[m];
			target_list = new int[m];
			degree = new int[n];
			s = reader.readLine();
			while (s != null && count < m) {
				if ("".equals(s)) {
					System.out.println("图加载出错！");
					break;
				}
				strs = s.split(",");
				int origin = Integer.parseInt(strs[0]);
				int target = Integer.parseInt(strs[1]);
				degree[origin]++;
				origin_list[count] = origin;
				target_list[count] = target;
				count++;
				s = reader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		for (int i = 0; i < n; i++) {
			g[i] = new int[degree[i]];
			degree[i] = 0;
		}

		for (int i = 0; i < m; i++) {
			g[origin_list[i]][degree[origin_list[i]]++] = target_list[i];
		}

		System.out.println("图加载成功！ 节点数量" + n + " 边数量：" + m);
		return g;
	}

	/**
	 * 从一个边文件中，加载图为的邻接矩阵形式，第一行为节点数量和边数量,此方法通常会内存不够
	 * 
	 * @param fileUrl
	 * @return
	 */
	public static int[][] getAdjMatrixGraphfromEdgesFileWithCount(String fileUrl) {
		int n = 0;
		int[][] g = null;
		FileReader in = null;
		LineNumberReader reader = null;
		int m = 0;
		try {
			in = new FileReader(fileUrl);
			reader = new LineNumberReader(in);
			String s = reader.readLine();
			String[] strs = s.split(",");
			if (!"".equals(strs[0])) {
				n = Integer.parseInt(strs[0]);
				m = Integer.parseInt(strs[1]);
			}
			g = new int[n][n];
			s = reader.readLine();
			while (s != null) {
				if ("".equals(s)) {
					System.out.println("图加载出错！");
					break;
				}
				strs = s.split(",");
				int origin = Integer.parseInt(strs[0]);
				int target = Integer.parseInt(strs[1]);
				g[origin][target] = 1;
				s = reader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		System.out.println("图加载成功！ 节点数量" + n + " 边数量：" + m);
		return g;
	}

	/**
	 * 从一个邻接表文件中加载图(邻接表)
	 * 
	 * @param fileUrl
	 * @return 二维邻接表
	 */
	public static int[][] getAdjTableGraphfromAdjTableFileWithoutCount(
			String fileUrl) {
		int n = getLineNum(fileUrl);
		int[][] g = new int[n][];
		FileReader in = null;
		LineNumberReader reader = null;
		int m = 0;
		try {
			in = new FileReader(fileUrl);
			reader = new LineNumberReader(in);
			String s = reader.readLine();
			while (s != null && m < n) {
				if ("".equals(s)) {
					s = reader.readLine();
					m++;
					continue;
				}
				String[] strs = s.split(",");
				g[m] = new int[strs.length];
				for (int i = 0; i < strs.length; i++) {
					g[m][i] = Integer.parseInt(strs[i]);
				}
				s = reader.readLine();
				m++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return g;
	}

	/**
	 * 从邻接表文件中获取行数，即节点数量
	 * 
	 * @param fileUrl
	 * @return
	 */
	public static int getLineNum(String fileUrl) {
		FileReader in = null;
		LineNumberReader reader = null;
		String s = null;
		int lines = 0;
		try {
			in = new FileReader(fileUrl);
			reader = new LineNumberReader(in);
			s = reader.readLine();
			while (s != null) {
				lines++;
				s = reader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return lines;
	}

	/**
	 * 从节点文件中加载节点集合，这里nodes.csv，只是一个用户ID的节点映射表，以行号为索引，改行内容为uId
	 * 
	 * @param nodesFileName
	 * @param g
	 * @return
	 */
	public static Node[] loadNodes(String nodesFileName, int g[][]) {
		FileReader in = null;
		LineNumberReader reader = null;
		Node[] vs = new Node[g.length];
		int i = 0;
		try {
			in = new FileReader(nodesFileName);
			reader = new LineNumberReader(in);
			String s = reader.readLine();// 跳过第一行字段名
			while ((s = reader.readLine()) != null) {
				if ("".equals(s)) {
					logger.error("读取到一段空的字符！");
				}
				String[] ss = s.split(",");
				Node v = initNode(i, ss);
				vs[i++] = v;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("节点文件加载成功！ 节点数量:" + i);
		return vs;
	}

	/**
	 * 生成一个节点
	 * 
	 * @param i
	 * @param g
	 * @return
	 */
	private static Node initNode(int i, String[] ss) {
		long userId = Long.parseLong(ss[0]);
		int fans = Integer.parseInt(ss[1]);
		int follow = Integer.parseInt(ss[2]);
		int degree = Integer.parseInt(ss[3]);
		int outDegree = Integer.parseInt(ss[4]);
		double bc = Double.parseDouble(ss[5]);
		double cc = Double.parseDouble(ss[6]);
		int kc = Integer.parseInt(ss[7]);
		Node v = new Node(i);
		v.setUserId(userId);
		v.setFans(fans);
		v.setFollow(follow);
		v.setDegree(degree);
		v.setOutDegree(outDegree);
		v.setBc(bc);
		v.setCc(cc);
		v.setKc(kc);
		// double transCap = Math.log((long) outDegree + 1) * UCSIR.MIU;
		double transCap = 1;
		v.setOutDegree(outDegree);
		v.setTc(transCap);
		v.setIc(transCap);
		v.setS(0);
		v.setIt(0);
		return v;
	}

	/**
	 * 返回图的入度邻接表
	 * 
	 * @param fileUrl
	 * @return
	 */
	public static int[][] getOutAdjTableGraphfromEdgesFileWithCount(
			String fileUrl) {
		int n = 0;
		int[][] g = null;
		FileReader in = null;
		LineNumberReader reader = null;
		int m = 0;
		int count = 0;
		int[] origin_list = null;
		int[] target_list = null;
		int[] degree = null;
		try {
			in = new FileReader(fileUrl);
			reader = new LineNumberReader(in);
			String s = reader.readLine();
			String[] strs = s.split(",");
			if (!"".equals(strs[0])) {
				n = Integer.parseInt(strs[0]);
				m = Integer.parseInt(strs[1]);
			}
			g = new int[n][];
			origin_list = new int[m];
			target_list = new int[m];
			degree = new int[n];
			s = reader.readLine();
			while (s != null && count < m) {
				if ("".equals(s)) {
					System.out.println("图加载出错！");
					break;
				}
				strs = s.split(",");
				int origin = Integer.parseInt(strs[0]);
				int target = Integer.parseInt(strs[1]);
				degree[target]++;
				origin_list[count] = origin;
				target_list[count] = target;
				count++;
				s = reader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		for (int i = 0; i < n; i++) {
			g[i] = new int[degree[i]];
			degree[i] = 0;
		}

		for (int i = 0; i < m; i++) {
			g[target_list[i]][degree[target_list[i]]++] = origin_list[i];
		}

		System.out.println("图加载成功！ 节点数量" + n + " 边数量：" + m);
		return g;
	}

}
