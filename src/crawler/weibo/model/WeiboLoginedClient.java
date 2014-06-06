package crawler.weibo.model;

import org.apache.http.client.HttpClient;

public class WeiboLoginedClient {
	HttpClient client;
	String userName;
	int reqCount;

	/**
	 * 初始化 请求次数为0
	 * 
	 * @param client
	 * @param userName
	 */
	public WeiboLoginedClient(HttpClient client, String userName) {
		this.client = client;
		this.userName = userName;
		this.reqCount = 0;
	}

	public HttpClient getClient() {
		return client;
	}

	public void setClient(HttpClient client) {
		this.client = client;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public int getReqCount() {
		return reqCount;
	}

	public void setReqCount(int reqCount) {
		this.reqCount = reqCount;
	}

	@Override
	public String toString() {
		return "weiboLoginedClient{userName:" + this.userName + ";reqCount:"
				+ this.reqCount + "}";
	}

}
