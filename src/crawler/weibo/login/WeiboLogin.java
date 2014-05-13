package crawler.weibo.login;

import java.io.IOException;
import java.net.URLDecoder;
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

import utils.BigIntegerRSA;
import utils.FileUtil;
import utils.HttpConnectionManager;
import crawler.weibo.model.PreLoginInfo;

/**
 * 
 * @author Joe
 * 
 */
public class WeiboLogin {

	public static String SINA_PK = "EB2A38568661887FA180BDDB5CABD5F21C7BFD59C090CB2D24"
			+ "5A87AC253062882729293E5506350508E7F9AA3BB77F4333231490F915F6D63C55FE2F08A49B353F444AD39"
			+ "93CACC02DB784ABBB8E42A9B1BBFFFB38BE18D78E87A0E41B9B8F73A928EE0CCEE"
			+ "1F6739884B9777E4FE9E88A1BBE495927AC4A799B3181D6442443";
	public static String username = FileUtil.getUname();
	public static String passwd = FileUtil.getPwd();
	public static String cookie = initCookieString();

	private static final Log logger = LogFactory.getLog(WeiboLogin.class);

	public static HttpClient getLoginStatus() {

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
		nvps.add(new BasicNameValuePair("su", encodeUserName(username)));
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
			cookie += "un=" + username;
			String entity = EntityUtils.toString(response.getEntity());
			if (entity.indexOf("retcode=4049") != -1) {
				String url = entity.substring(
						entity.indexOf("location.replace(") + 18,
						entity.indexOf("\");"));
				System.out.println(url);
				HttpGet getMethod = new HttpGet(url);
				response = client.execute(getMethod);
				entity = EntityUtils.toString(response.getEntity());
				System.out.println("需要输入验证码！");
				System.out.println(entity);
			}
			if (entity.indexOf("code=0") == -1) {
				logger.error("登陆失败:"
						+ URLDecoder.decode(entity.substring(
								entity.indexOf("reason=") + 7,
								entity.indexOf("&#39;\"/>"))));
				System.out.println("登陆失败:"
						+ URLDecoder.decode(entity.substring(
								entity.indexOf("reason=") + 7,
								entity.indexOf("&#39;\"/>"))));
				return null;
			}

			String url = entity.substring(
					entity.indexOf("http://weibo.com/ajaxlogin.php?"),
					entity.indexOf("code=0") + 6);

			HttpGet getMethod = new HttpGet(url);
			response = client.execute(getMethod);
			entity = EntityUtils.toString(response.getEntity());
			logger.info("用户名:"
					+ username
					+ "登陆成功！\n"
					+ entity.substring(entity.indexOf("({"),
							entity.indexOf(";</script>")));
			System.out.println("用户名:"
					+ username
					+ "登陆成功！\n"
					+ entity.substring(entity.indexOf("({"),
							entity.indexOf(";</script>")));
			/*
			 * HttpGet getMethod1 = new
			 * HttpGet("http://weibo.com/1196235387/info"); response =
			 * client.execute(getMethod1); entity =
			 * EntityUtils.toString(response.getEntity());
			 * System.out.println(entity);
			 */

		} catch (ParseException e) {
			e.printStackTrace();
			logger.error(e);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			logger.error(e);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e);
		}
		return client;
	}

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
