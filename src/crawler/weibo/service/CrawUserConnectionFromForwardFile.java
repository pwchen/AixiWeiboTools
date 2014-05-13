package crawler.weibo.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

public class CrawUserConnectionFromForwardFile {

	public static void main(String[] args) {
		String msgUrl = "1762325394/AeAwhqd2m";
		msgUrl = msgUrl.replace("/", "\\");
		String url = "G:\\data\\forward\\" + msgUrl + "\\retweetedusers.csv";
		crawlUserConnection(url);
		// ExtractNodesFromOracle.main(new String[] { "msgUrl", msgUrl });
	}

	/**
	 * 爬用户关系：读取转发文件，根据每个ID爬取其200个关注用户和200个粉丝
	 * 
	 * @param client
	 * @param url
	 */
	public static void crawlUserConnection(String url) {
		Long[] forwardIdArr = loadForwardIdArr(url);
		new CrawlUserConnection(forwardIdArr, forwardIdArr.length)
				.getUserConnection();
	}

	/**
	 * 从转发文件中读取所有的转发用户id
	 * 
	 * @param url
	 * @return
	 */
	private static Long[] loadForwardIdArr(String url) {
		List<Long> userIdList = new ArrayList<Long>();
		File file = new File(url);
		FileReader fr = null;
		LineNumberReader lnr = null;
		try {
			fr = new FileReader(file);
			lnr = new LineNumberReader(fr);
			String s = lnr.readLine();
			while ((s = lnr.readLine()) != null) {
				String[] ss = s.split(",");
				addIdtoUserIdList(userIdList, ss[0]);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fr.close();
				lnr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Long[] a = new Long[userIdList.size()];
		return userIdList.toArray(a);
	}

	/**
	 * 将id保存到UserIdList里面，如果id已存在，则不保存
	 * 
	 * @param userIdList
	 * @param string
	 */
	private static void addIdtoUserIdList(List<Long> userIdList, String idStr) {
		if (idStr != null && !"".equals(idStr)) {
			long uid = Long.parseLong(idStr);
			if (!userIdList.contains(uid)) {
				userIdList.add(uid);
			}
		}
	}

}