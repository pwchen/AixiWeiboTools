package crawler.weibo.login;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
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

import utils.FileUtils;
import utils.PropertyUtils;

/**
 * 
 * @author Joe
 * 
 */
public class WeiboLoginHttpClientUtils {

	public static String SINA_PK = "EB2A38568661887FA180BDDB5CABD5F21C7BFD59C090CB2D24"
			+ "5A87AC253062882729293E5506350508E7F9AA3BB77F4333231490F915F6D63C55FE2F08A49B353F444AD39"
			+ "93CACC02DB784ABBB8E42A9B1BBFFFB38BE18D78E87A0E41B9B8F73A928EE0CCEE"
			+ "1F6739884B9777E4FE9E88A1BBE495927AC4A799B3181D6442443";
	public static String[] userNameList = PropertyUtils.getUname().split(",");
	public static String passwd = PropertyUtils.getPwd();
	public static int maxReqCount = PropertyUtils.getReqcount();
	public static int failureCount = PropertyUtils.getFailureCount();
	public static String cookie = initCookieString();
	public static HttpClient httpClient = null;
	public static int uNum = 0;
	public static boolean expireClient = false;// 登陆过期
	public static String userName = userNameList[uNum];
	public static int reqCount = 0;

	private static final Log logger = LogFactory
			.getLog(WeiboLoginHttpClientUtils.class);

	public static void main(String[] args) {
		for (int i = 0; i < 2000; i++) {
			getLoginhttpClient();
		}
	}

	public synchronized static HttpClient getLoginhttpClient() {
		if (httpClient == null) {
			int i = 0;
			while (i++ < failureCount) {
				httpClient = getLoginStatus();
				reqCount = 0;
				expireClient = false;
				if (httpClient != null) {
					break;
				}
				if (++uNum >= userNameList.length) {
					uNum = 0;
				}
				userName = userNameList[uNum];
			}
		}
		if (++reqCount >= maxReqCount) {// 请求次数超过最大请求，换号
			maxReqCount = (int) (PropertyUtils.getReqcount() - Math.random() * 200);
			expireClient = true;
			return changeLoginAccount();
		}
		return httpClient;
	}

	public synchronized static HttpClient changeLoginAccount() {
		if (expireClient) {
			logger.error("当前请求次数reqCount:" + reqCount);
			httpClient = null;
			if (++uNum >= userNameList.length) {
				uNum = 0;
			}
			userName = userNameList[uNum];
		}
		return getLoginhttpClient();
	}

	private static HttpClient getLoginStatus() {

		final HttpClient client = HttpConnectionManager.getHttpClient();
		HttpPost post = new HttpPost(
				"http://login.sina.com.cn/sso/login.php?client=ssologin.js(v1.4.2)");

		PreLoginInfo info = null;
		try {
			info = getPreLoginBean(client);
		} catch (HttpException e) {
			e.printStackTrace();
			logger.error(e);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("登陆失败，请确认已连接正确网络！" + e);
			return null;
		} catch (JSONException e) {
			e.printStackTrace();
			logger.error(e);
		}

		long servertime = info.servertime;
		String nonce = info.nonce;

		String pwdString = servertime + "\t" + nonce + "\n" + passwd;
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
			String cookiestr;
			for (int i = 0; i < response.getHeaders("Set-Cookie").length; i++) {
				cookiestr = response.getHeaders("Set-Cookie")[i].toString()
						.replace("Set-Cookie:", "").trim();
				cookie = cookiestr.substring(0, cookiestr.indexOf(";")) + ";";
			}
			cookie += "un=" + userName;
			String entity = EntityUtils.toString(response.getEntity());
			if (entity.indexOf("retcode=4049") != -1) {
				logger.error(userName + "登陆失败！需要输入验证码！系统即将退出！");
				// FileUtils.shutDoun();//关机
				System.exit(1);
			}
			if (entity.indexOf("code=0") == -1) {
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
			logger.error(e);
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
	public static String getPreLoginInfo(HttpClient client)
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
		// System.out.println(jsonBody);
		return jsonBody;

	}

	/**
	 * 获取制定url的网页内容
	 * 
	 * @param client
	 * @param personalUrl
	 * @return
	 */
	public static String getRawHtml(String url) {
		HttpClient client = getLoginhttpClient();
		HttpGet getMethod = new HttpGet(url);
		String entityStr = null;
		InputStream in = null;
		int count = 0;
		boolean ex = true;// 出异常
		while (count++ < failureCount && ex) {
			ex = false;
			try {
				HttpResponse response = client.execute(getMethod);
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					in = entity.getContent(); // 之前没使用这个造成了大量异常抛出，只要是
				}
				entityStr = EntityUtils.toString(entity, "utf-8");
			} catch (IOException e) {
				ex = true;
				logger.warn(e);
				continue;
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			if (entityStr.indexOf("抱歉，网络繁忙") != -1) {
				expireClient = true;
				changeLoginAccount();
				logger.error("抱歉，网络繁忙:" + url);
				ex = true;
			} else if (entityStr.indexOf("正确输入验证码答案") != -1) {
				expireClient = true;
				changeLoginAccount();
				logger.error("正确输入验证码答案:" + url);
				ex = true;
			} else if (entityStr
					.indexOf("æ°æµªå¾®å-éæ¶éå°åäº«èº«è¾¹çæ°é²äºå¿") != -1) {
				FileUtils.saveToFile(entityStr, "tempuft.html", "utf-8");
				FileUtils.saveToFile(entityStr, "tempgbk.html", "gbk");
				logger.error("乱码异常:æ°æµªå¾®å-éæ¶éå°åäº«èº«è¾¹çæ°é²äºå¿"
						+ url);
				try {
					Thread.sleep(6000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return entityStr;
			} else if (entityStr.indexOf("您当前访问的帐号异常") != -1) {
				expireClient = true;
				changeLoginAccount();
				logger.error("您当前访问的帐号异常:" + url);
				ex = true;
			} else if (entityStr
					.indexOf("The server returned an invalid or incomplete response") != -1) {
				expireClient = true;
				changeLoginAccount();
				logger.error("502 Bad Gateway,The server returned an invalid or incomplete response:"
						+ url);
				ex = true;
			} else if (entityStr.indexOf("页面地址有误") != -1) {
				logger.warn("页面地址有误，或者该页面不存在:" + url);
				return null;
			}
		}
		if (count > failureCount) {
			logger.error("超时次数超过最大次数:" + failureCount + "放弃此次请求。");
		} else if (entityStr == null) {
			logger.error("未知原因:" + url);
		}
		return entityStr;
	}

	private static String getCurrentTime() {
		long servertime = new Date().getTime() / 1000;
		return String.valueOf(servertime);
	}

	private static String encodeUserName(String email) {
		email = email.replaceFirst("@", "%40");// MzM3MjQwNTUyJTQwcXEuY29t
		email = Base64.encodeBase64String(email.getBytes());
		return email;
	}

	private static String initCookieString() {
		StringBuilder sb = new StringBuilder();
		sb.append("wvr=4;");
		return sb.toString();
	}
}
