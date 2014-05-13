package crawler.weibo.test;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.json.JSONException;

import utils.FileUtil;
import crawler.weibo.console.ConsoleTextArea;
import crawler.weibo.login.WeiboLogin;
import crawler.weibo.thread.GetMessageMain;
import crawler.weibo.thread.GetUserInfoThread;

public class CrawlerMain extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8169976610203386642L;
	/**
	 * 
	 */
	private static TrayIcon trayIcon = null;
	static SystemTray tray = SystemTray.getSystemTray();
	ImageIcon trayImg = new ImageIcon("img/ico.jpg");// 托盘图标
	private static final Log logger = LogFactory.getLog(CrawlerMain.class);

	public static void main(String[] args) throws JSONException, IOException {
		// new
		// UserJdbcService().addUserIDfromQueue(FileUtil.getFirstId());//第一次从配置中读取入口id
		// System.out.println(GetWeiboUsersMain.class.getResource(""));
		CrawlerMain frame = new CrawlerMain();
		frame.init();
		frame.crawl();
	}

	private void crawl() {
		HttpClient client = WeiboLogin.getLoginStatus();
		int failureCount = 0;
		while (client == null) {
			if (failureCount++ > FileUtil.getFailureCount()) {
				System.out.println(Thread.currentThread().getName()
						+ "连接失败次数已经超过最大连接数！程序关闭......");
				logger.error("连接失败次数已经超过最大连接数！程序20秒后关闭......");
				try {
					Thread.sleep(20000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.exit(1);
			}
			System.out.println(Thread.currentThread().getName()
					+ "连接网络失败！20秒后重新连接......");
			logger.error("连接网络失败！20秒后重新连接......");
			try {
				Thread.sleep(20000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			client = WeiboLogin.getLoginStatus();
		}

		if ("1".equals(FileUtil.getMode())) {
			new GetMessageMain().startGetMessage(client);
		} else if ("0".equals(FileUtil.getMode())) {
			GetUserInfoThread gut = new GetUserInfoThread(client);
			int threadCount = FileUtil.getThreadCount();
			for (int i = 0; i < threadCount; i++) {
				new Thread(gut).start();
			}
		}
	}

	private void init() {
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		ConsoleTextArea console = null;
		try {
			console = new ConsoleTextArea();
		} catch (IOException e) {
			e.printStackTrace();
		}
		JScrollPane scrollPane = new JScrollPane(console);
		Container content = getContentPane();
		content.add(scrollPane, BorderLayout.CENTER);
		setLocation(250, 50);
		setSize(600, 400);
		setVisible(true);
		setIconImage(trayImg.getImage());
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				int i = JOptionPane.showConfirmDialog(null, "确定要退出采集器吗？",
						"退出系统", JOptionPane.YES_NO_OPTION);
				if (i == JOptionPane.YES_OPTION) {
					System.exit(0);
				}
			}

			public void windowIconified(WindowEvent e) { // 窗口最小化事件
				setVisible(false);
				miniTray();
			}
		});
	}

	private void miniTray() { // 窗口最小化到任务栏托盘
		PopupMenu pop = new PopupMenu(); // 增加托盘右击菜单
		MenuItem show = new MenuItem("show");
		MenuItem exit = new MenuItem("exit");
		show.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { // 按下还原键
				tray.remove(trayIcon);
				setVisible(true);
				setExtendedState(JFrame.NORMAL);
				toFront();
			}
		});
		exit.addActionListener(new ActionListener() { // 按下退出键
			public void actionPerformed(ActionEvent e) {
				int i = JOptionPane.showConfirmDialog(null, "确定要退出采集器吗？",
						"退出系统", JOptionPane.YES_NO_OPTION);
				if (i == JOptionPane.YES_OPTION) {
					tray.remove(trayIcon);
					System.exit(0);
				}
			}
		});
		pop.add(show);
		pop.add(exit);
		trayIcon = new TrayIcon(trayImg.getImage(), "阿蒙微博采集器", pop);
		trayIcon.setImageAutoSize(true);
		trayIcon.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) { // 鼠标器双击事件
				if (e.getClickCount() == 2) {
					tray.remove(trayIcon); // 移去托盘图标
					setVisible(true);
					setExtendedState(JFrame.NORMAL); // 还原窗口
					toFront();
				}
			}
		});
		try {
			tray.add(trayIcon);
		} catch (AWTException e1) {
			e1.printStackTrace();
		}
	}
}
