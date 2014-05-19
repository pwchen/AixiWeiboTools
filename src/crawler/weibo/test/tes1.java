package crawler.weibo.test;

import java.util.ArrayList;

public class tes1 {

	public static void main(String[] args) {
		String[] userArr1 = { "1", "2", "3", "4", "5" };
		String[] userArr2 = { "1", "2", "3", "4", "5", "1412" };
		int userArr1Len = userArr1.length;
		ArrayList<String> userList = new ArrayList<String>();
		for (int i = 0; i < userArr1Len; i++)
			userList.add(userArr1[i]);
		for (int i = 0; i < userArr2.length; i++) {// 去重
			boolean flag = true;
			for (int j = 0; j < userArr1Len; j++) {
				if (userArr1[j] == userArr2[i]) {
					flag = false;
					break;
				}
			}
			if (flag) {
				userList.add(userArr2[i]);
			}
		}
		for (String u : userList.toArray(userArr1))
			System.out.println(u);
	}

}
