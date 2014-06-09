package utils;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import crawler.weibo.service.login.HttpConnectionManager;

/**
 * 用户管理宽带连接的类
 * 
 * @author Administrator
 * 
 */
public class ConnectNetworkUtil {
	private static final Log logger = LogFactory
			.getLog(ConnectNetworkUtil.class);

	/**
	 * 执行CMD命令,并返回String字符串
	 */
	public static String executeCmd(String strCmd) throws Exception {
		Process p = Runtime.getRuntime().exec("cmd /c " + strCmd);
		StringBuilder sbCmd = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				p.getInputStream(), "GBK"));
		String line;
		while ((line = br.readLine()) != null) {
			sbCmd.append(line + "\n");
		}
		return sbCmd.toString();
	}

	/**
	 * 连接ADSL
	 * 
	 * @param connName
	 *            连接名称
	 * @param userName
	 *            用户名
	 * @param password
	 *            密码
	 * @return
	 */
	public static boolean connAdsl(String connName, String userName,
			String password) {
		logger.info("正在建立连接.");
		String adslCmd = "rasdial " + connName + " " + userName + " "
				+ password;
		String tempCmd = null;
		try {
			tempCmd = executeCmd(adslCmd);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 判断是否连接成功
		if (tempCmd.indexOf("已连接") > 0) {
			logger.info("已成功建立连接.");
			return true;
		} else {
			logger.error("建立连接失败:" + tempCmd);
			return false;
		}
	}

	/**
	 * @param connName
	 *            连接名称
	 * @return
	 */
	public static boolean cutAdsl(String connName) throws Exception {
		logger.info("正在断开连接.");
		String cutAdsl = "rasdial " + connName + " /disconnect";
		String result = executeCmd(cutAdsl);

		if (result.indexOf("没有连接") != -1) {
			logger.warn(connName + "连接不存在!");
			return false;
		} else {
			logger.info("连接已断开");
			return true;
		}
	}

	/**
	 * 重接ADSL
	 * 
	 * @param connName
	 *            连接名称
	 * @param userName
	 *            用户名
	 * @param password
	 *            密码
	 * @return
	 */
	public static boolean reConnAdsl(String connName, String userName,
			String password) {
		logger.info("重新连接ADSL..");
		try {
			cutAdsl(connName);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		try {
			Thread.sleep(3000);// 一般断开ADSL具有重连时间
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		int failureNumber = 0;
		while (failureNumber < 10) {// 允许最大的重连失败10次
			try {
				if (connAdsl(connName, userName, password)) {
					return true;
				} else {
					failureNumber++;
					logger.warn("已经失败" + (failureNumber + 1)
							+ "次，等待5秒后重新连接ADSL..");
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		logger.error("重新连接ADSL次数已经超最大失败次数....");
		return false;
	}

	/**
	 * 待完善ADSL
	 * 
	 * @return
	 */
	public static boolean reConnAdslbyRout() {
		logger.info("重新连接ADSL..");
		DefaultHttpClient client = (DefaultHttpClient) HttpConnectionManager
				.getHttpClient();
		HttpPost post = new HttpPost(
				"http://192.168.199.1/cgi-bin/turbo/admin_web");
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("username", "admin"));
		nvps.add(new BasicNameValuePair("password", "!*^)))#&%#)"));
		try {
			post.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			HttpResponse response = client.execute(post);
			String entity = EntityUtils.toString(response.getEntity());
			Header[] setCookie = response.getHeaders("Set-Cookie");
			// ------------
			String stok = "";
			if (setCookie == null) {
				System.out.println("None");
			} else {
				System.out.println(setCookie);
				String[] cookies = setCookie[0].getValue().split(";");
				for (int i = 0; i < cookies.length; i++) {
					if (cookies[i].indexOf("stok") != -1) {
						stok = cookies[i];
					}
				}
			}
			// --------------

			logger.info(stok);
			// 关闭 sysauth=b06a976925c243f1e7c2c60c97836dc3
			// a687267a7746ceb40173c3c2d18d9bca d061053dc5c2ded46909570ce677ae8a
			// d9071d6a5e068a0d6535be06861ac2eb
			String url = "http://192.168.199.1/cgi-bin/turbo/;" + stok
					+ "/api/network/wan_shutdown";
			HttpGet getMethod = new HttpGet(url);
			response = client.execute(getMethod);
			entity = EntityUtils.toString(response.getEntity());
			JSONObject jsonObject = null;
			try {
				jsonObject = new JSONObject(entity);
				if (jsonObject.getInt("code") == 0) {
					logger.info("ADSL关闭成功！");
				} else {
					logger.error("ADSL关闭失败！" + entity);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			// 重启
			url = "http://192.168.199.1/cgi-bin/turbo/;" + stok
					+ "/api/network/wan_reconect";
			getMethod = new HttpGet(url);
			response = client.execute(getMethod);
			entity = EntityUtils.toString(response.getEntity());
			try {
				jsonObject = new JSONObject(entity);
				if (jsonObject.getInt("code") == 0) {
					logger.info("ADSL重启成功！");
				} else {
					logger.error("ADSL重启失败！" + entity);
					return false;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} catch (ParseException e) {
			logger.error(e);
			return false;
		} catch (ClientProtocolException e) {
			logger.error(e);
			return false;
		} catch (IOException e) {
			logger.warn(e);
			return false;
		}
		return true;
	}

	public static void main(String[] args) throws Exception {

		reConnAdslbyRout();
		// reConnAdsl("宽带", "hzhz**********", "******");
		// Thread.sleep(1000);
		// cutAdsl("宽带");
		// Thread.sleep(1000);
		// 再连，分配一个新的IP
		// connAdsl("宽带", "hzhz**********", "******");
	}
}
