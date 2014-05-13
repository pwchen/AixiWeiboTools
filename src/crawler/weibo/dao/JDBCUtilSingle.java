package crawler.weibo.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class JDBCUtilSingle {
	static String url = "jdbc:oracle:thin:@localhost:1521:weibo";
	static String name = "weibo";
	static String password = "111111";
	static Connection conn = null;
	private static JDBCUtilSingle jdbcUtilSingle = null;
	private static final Log logger = LogFactory.getLog(JDBCUtilSingle.class);

	public static JDBCUtilSingle getInitJDBCUtil() {
		if (jdbcUtilSingle == null) {
			synchronized (JDBCUtilSingle.class) {
				if (jdbcUtilSingle == null) {
					jdbcUtilSingle = new JDBCUtilSingle();
				}
			}
		}
		return jdbcUtilSingle;
	}

	private JDBCUtilSingle() {
	}

	static {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public Connection getConnection() {
		logger.info(Thread.currentThread().getName() + "建立连接....");
		System.out.println(Thread.currentThread().getName() + "建立连接....");
		if (conn == null) {
			try {
				conn = DriverManager.getConnection(url, name, password);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return conn;

	}

	public void closeConnection(ResultSet rs, Statement statement,
			Connection con) {
		try {
			if (rs != null) {
				rs.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (con != null) {
						con.close();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
