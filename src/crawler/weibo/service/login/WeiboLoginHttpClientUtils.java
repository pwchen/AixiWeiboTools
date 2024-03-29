package crawler.weibo.service.login;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import utils.CrawlerContext;
import utils.FileUtils;
import crawler.weibo.model.WeiboLoginedClient;

/**
 * 微博登陆器，根据最大次数自动换号
 * 
 * @author Joe
 * 
 */
public class WeiboLoginHttpClientUtils {

	public static String SINA_PK = "EB2A38568661887FA180BDDB5CABD5F21C7BFD59C090CB2D24"
			+ "5A87AC253062882729293E5506350508E7F9AA3BB77F4333231490F915F6D63C55FE2F08A49B353F444AD39"
			+ "93CACC02DB784ABBB8E42A9B1BBFFFB38BE18D78E87A0E41B9B8F73A928EE0CCEE"
			+ "1F6739884B9777E4FE9E88A1BBE495927AC4A799B3181D6442443";
	public static String[] userNameList = CrawlerContext.getContext()
			.getUserNameList();
	public static String commonPwd = CrawlerContext.getContext().getCommonPwd();
	public static int requestNumber = CrawlerContext.getContext()
			.getRequestNumber();// 最大请求次数
	public static int failureCount = CrawlerContext.getContext()
			.getFailureNumber();// 最大连接失败的次数
	public static int currentUserIndex = 0; // 当前用户的编号

	private static final Log logger = LogFactory
			.getLog(WeiboLoginHttpClientUtils.class);

	public static void main(String[] args) {
		for (int i = 0; i < 1; i++) {
			System.out.println(getWeiboLoginedClient());
		}
	}

	/**
	 * 获取一个已经登陆的客户端
	 * 
	 * @return HttpClient
	 */
	public synchronized static WeiboLoginedClient getWeiboLoginedClient() {
		HttpClient httpClient = null;
		int i = 0;
		while (i++ < failureCount) {
			String userName = userNameList[currentUserIndex++];
			if (currentUserIndex >= userNameList.length) {// 换下一个用户
				currentUserIndex = 0;
			}
			httpClient = getLoginStatus(userName, commonPwd);
			if (httpClient != null) {
				WeiboLoginedClient wlClient = new WeiboLoginedClient(
						httpClient, userName);
				return wlClient;
			}
		}
		return null;
	}

	/**
	 * 登陆过程
	 * 
	 * @return
	 */
	private static HttpClient getLoginStatus(String userName, String password) {

		HttpClient client = HttpConnectionManager.getHttpClient();
		HttpPost post = new HttpPost(
				"http://login.sina.com.cn/sso/login.php?client=ssologin.js(v1.4.2)");

		PreLoginInfo info = null;
		try {
			info = getPreLoginBean(client);
		} catch (HttpException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error("登陆失败，请确认已连接正确网络！" + e);
			return null;
		} catch (JSONException e) {
			logger.error(e);
		}

		long servertime = info.servertime;
		String nonce = info.nonce;

		String pwdString = servertime + "\t" + nonce + "\n" + password;
		String sp = new BigIntegerRSA().rsaCrypt(SINA_PK, "10001", pwdString);

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("entry", "weibo"));
		nvps.add(new BasicNameValuePair("gateway", "1"));
		nvps.add(new BasicNameValuePair("from", ""));
		nvps.add(new BasicNameValuePair("savestate", "7"));
		nvps.add(new BasicNameValuePair("useticket", "1"));
		nvps.add(new BasicNameValuePair("vsnf", "1"));
		nvps.add(new BasicNameValuePair("ssosimplelogin", "1"));
		nvps.add(new BasicNameValuePair("vsnval", ""));
		nvps.add(new BasicNameValuePair("su", encodeUserName(userName)));
		nvps.add(new BasicNameValuePair("service", "miniblog"));
		nvps.add(new BasicNameValuePair("servertime", servertime + ""));
		nvps.add(new BasicNameValuePair("nonce", nonce));
		nvps.add(new BasicNameValuePair("pwencode", "rsa2"));
		nvps.add(new BasicNameValuePair("rsakv", info.rsakv));
		nvps.add(new BasicNameValuePair("sp", sp));
		nvps.add(new BasicNameValuePair("encoding", "UTF-8"));
		nvps.add(new BasicNameValuePair("prelt", "91"));
		nvps.add(new BasicNameValuePair("returntype", "META"));
		nvps.add(new BasicNameValuePair(
				"url",
				"http://weibo.com/ajaxlogin.php?framelogin=1&callback=parent.sinaSSOController.feedBackUrlCallBack"));

		try {
			post.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			HttpResponse response = client.execute(post);
			/*
			 * String cookiestr; for (int i = 0; i <
			 * response.getHeaders("Set-Cookie").length; i++) { cookiestr =
			 * response.getHeaders("Set-Cookie")[i].toString()
			 * .replace("Set-Cookie:", "").trim();
			 * System.out.println("--------------");
			 * System.out.println(cookiestr);
			 * System.out.println("--------------"); }
			 */
			String entity = EntityUtils.toString(response.getEntity());
			if (entity.indexOf("retcode=4049") != -1) {
				logger.warn(userName + "登陆失败！需要输入验证码！重新拨号以验证IP！");
				try {
					Thread.sleep(100000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// ConnectNetworkUtil.reConnAdsl("FZFTTH", "02007136612",
				// "UPNMVHLJ");
			}
			if (entity.indexOf("code=0") == -1) {
				String fileName = "loginFail" + new Date().getTime() + ".html";
				logger.error("登陆失败,原因保存至:" + fileName);
				FileUtils.saveToFile(entity, fileName, "utf-8");
				return null;
			}
			if (entity.indexOf("UG020908") != -1) {
				String fileName = "loginFail" + new Date().getTime() + ".html";
				logger.error("登陆失败,原因保存至:" + fileName);
				FileUtils.saveToFile(entity, fileName, "utf-8");
				return null;
			}

			String url = entity.substring(
					entity.indexOf("http://weibo.com/ajaxlogin.php?"),
					entity.indexOf("code=0") + 6);

			HttpGet getMethod = new HttpGet(url);
			response = client.execute(getMethod);
			entity = EntityUtils.toString(response.getEntity());
			String s = entity.substring(entity.indexOf("{"),
					entity.indexOf(");</script>"));
			try {
				JSONObject jsonObject = new JSONObject(s);
				s = jsonObject.toString();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			logger.info("用户名:" + userName + "登陆成功！\n" + s);
		} catch (ParseException e) {
			logger.error(e);
			return null;
		} catch (ClientProtocolException e) {
			logger.error(e);
			return null;
		} catch (IOException e) {
			logger.warn(e);
			return null;
		}
		return client;
	}

	/**
	 * 封装登录参数
	 * 
	 * @param client
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 * @throws JSONException
	 */
	private static PreLoginInfo getPreLoginBean(HttpClient client)
			throws HttpException, IOException, JSONException {

		String serverTime = getPreLoginInfo(client);
		JSONObject jsonInfo = new JSONObject(serverTime);
		PreLoginInfo info = new PreLoginInfo();
		info.nonce = jsonInfo.getString("nonce");
		info.pcid = jsonInfo.getString("pcid");
		info.pubkey = jsonInfo.getString("pubkey");
		info.retcode = jsonInfo.getInt("retcode");
		info.rsakv = jsonInfo.getString("rsakv");
		info.servertime = jsonInfo.getLong("servertime");
		return info;
	}

	/**
	 * 将登录参数整合成url
	 * 
	 * @param client
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	private static String getPreLoginInfo(HttpClient client)
			throws ParseException, IOException {
		String preloginurl = "http://login.sina.com.cn/sso/prelogin.php?entry=sso&"
				+ "callback=sinaSSOController.preloginCallBack&su="
				+ "dW5kZWZpbmVk"
				+ "&rsakt=mod&client=ssologin.js(v1.4.2)"
				+ "&_=" + getCurrentTime();
		HttpGet get = new HttpGet(preloginurl);

		HttpResponse response = client.execute(get);

		String getResp = EntityUtils.toString(response.getEntity());

		int firstLeftBracket = getResp.indexOf("(");
		int lastRightBracket = getResp.lastIndexOf(")");

		String jsonBody = getResp.substring(firstLeftBracket + 1,
				lastRightBracket);
		return jsonBody;

	}

	/**
	 * 获取当前时间戳
	 * 
	 * @return
	 */
	private static String getCurrentTime() {
		long servertime = new Date().getTime() / 1000;
		return String.valueOf(servertime);
	}

	/**
	 * 对用户名加密
	 * 
	 * @param email
	 * @return
	 */
	private static String encodeUserName(String un) {
		un = un.replaceFirst("@", "%40");// MzM3MjQwNTUyJTQwcXEuY29t
		un = Base64.encodeBase64String(un.getBytes());
		return un;
	}

}
