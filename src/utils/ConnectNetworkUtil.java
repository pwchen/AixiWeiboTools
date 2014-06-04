package utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * 用户管理宽带连接的类
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
			String password) throws Exception {
		logger.info("正在建立连接.");
		String adslCmd = "rasdial " + connName + " " + userName + " "
				+ password;
		String tempCmd = executeCmd(adslCmd);
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

	public static void main(String[] args) throws Exception {
		reConnAdsl("宽带", "hzhz**********", "******");
		// Thread.sleep(1000);
		// cutAdsl("宽带");
		// Thread.sleep(1000);
		// 再连，分配一个新的IP
		// connAdsl("宽带", "hzhz**********", "******");
	}
}
