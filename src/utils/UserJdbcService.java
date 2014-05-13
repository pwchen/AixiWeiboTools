package utils;

import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import oracle.sql.CLOB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import crawler.weibo.model.WeiboUser;

public class UserJdbcService {
	private static final Connection oconn = JDBCUtilSingle.getInitJDBCUtil()
			.getConnection();
	private static final Log logger = LogFactory.getLog(UserJdbcService.class);
	private static UserJdbcService userJdbcService = null;

	public static UserJdbcService getInstance() {
		if (userJdbcService == null) {
			synchronized (UserJdbcService.class) {
				if (userJdbcService == null) {
					userJdbcService = new UserJdbcService();
				}
			}
		}
		return userJdbcService;
	}

	public synchronized List<String> getUserIDfromUser() {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<String> list = null;
		String sql = "select userid from T_WEIBO_CRAWLER_USER t where t.follow_num<500 and t.message_num>5000";
		try {
			pstmt = oconn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			list = new ArrayList<String>();
			while (rs.next()) {
				list.add(rs.getString("userid"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e);
		} finally {
			try {
				rs.close();
				pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	/**
	 * 从用户ID队列中取出最早的一条
	 * 
	 * @return
	 */
	public synchronized String getUserIDfromQueue() {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "select userid from (select userid from T_WEIBO_CRAWLER_QUEUE t where t.is_success='0' order by t.update_time) where rownum=1";
		try {
			pstmt = oconn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				String userId = rs.getString("userid");
				sql = "update T_WEIBO_CRAWLER_QUEUE t set t.is_success='1' where t.userid='"
						+ userId + "'";
				pstmt.close();
				pstmt = oconn.prepareStatement(sql);
				pstmt.executeUpdate();
				return userId;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e);
		} finally {
			try {
				rs.close();
				pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 将队列中is_success置为0
	 * 
	 * @param userId
	 */
	public synchronized void recoverUserIDfromQueue(String userId) {
		PreparedStatement pstmt = null;
		String sql = "update T_WEIBO_CRAWLER_QUEUE t set t.is_success='0',t.update_time=? where t.userid='"
				+ userId + "'";
		try {
			pstmt = oconn.prepareStatement(sql);
			pstmt.setTimestamp(1, new Timestamp(new Date().getTime()));
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e);
		} finally {
			try {
				pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 更新列队中的用户id，is_success置为0，updatenum+1
	 * 
	 * @param userId
	 */
	public synchronized void updateUserIdfomQueue(String userId) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "select * from T_WEIBO_CRAWLER_QUEUE t where t.userid='"
				+ userId + "'";
		int updateNum = 0;
		try {
			pstmt = oconn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				updateNum = rs.getInt("UPDATE_NUM");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e);
		} finally {
			try {
				rs.close();
				pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		sql = "update T_WEIBO_CRAWLER_QUEUE t set t.is_success='0',t.update_num=?,t.update_time=? where t.userid=?";
		try {
			pstmt = oconn.prepareStatement(sql);
			pstmt.setInt(1, updateNum + 1);
			pstmt.setTimestamp(2, new Timestamp(new Date().getTime()));
			pstmt.setString(3, userId);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e);
		} finally {
			try {
				pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 向队列中添加一个用户ID
	 * 
	 * @param userId
	 */
	public synchronized void addUserIDfromQueue(String userId) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "select userid from T_WEIBO_CRAWLER_QUEUE t where t.userid='"
				+ userId + "'";
		try {
			pstmt = oconn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if (!rs.next()) {
				rs.close();
				pstmt.close();
				sql = "insert into T_WEIBO_CRAWLER_QUEUE (userid,create_time,update_time) values (?,?,?)";
				pstmt = oconn.prepareStatement(sql);
				pstmt.setString(1, userId);
				Timestamp timestamp = new Timestamp(
						new java.util.Date().getTime());
				pstmt.setTimestamp(2, timestamp);
				pstmt.setTimestamp(3, timestamp);
				pstmt.executeUpdate();
				return;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e);
		} finally {
			try {
				rs.close();
				pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean needUpdate(WeiboUser oWeiboUser, WeiboUser weiboUser) {
		int oldSum = oWeiboUser.getMessageNum() + oWeiboUser.getFollowNum();
		int newSum = weiboUser.getMessageNum() + weiboUser.getFollowNum();
		return newSum > oldSum;
	}

	public synchronized WeiboUser getWeiboUser(String userId) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		if (userId != null) {
			String sql = "select * from t_weibo_crawler_user t where t.userid='"
					+ userId + "'";
			try {
				pstmt = oconn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					WeiboUser weiboUser = new WeiboUser();
					weiboUser.setUserId(userId);
					weiboUser.setScreenName(rs.getString("SCREEN_NAME"));
					weiboUser.setSex(rs.getString("SEX"));
					weiboUser.setDescription(rs.getString("DESCRIPTION"));
					weiboUser.setUserName(rs.getString("USERNAME"));
					weiboUser.setFollowNum(rs.getInt("FOLLOW_NUM"));
					weiboUser.setFansNum(rs.getInt("FANS_NUM"));
					weiboUser.setMessageNum(rs.getInt("MESSAGE_NUM"));
					weiboUser.setProfileImageUrl(rs
							.getString("PROFILE_IMAGE_URL"));
					weiboUser.setIsVerified(rs.getString("IS_VERIFIED"));
					weiboUser.setEducationInfo(rs.getString("EDUCATION_INFO"));
					weiboUser.setTag(rs.getString("TAG"));
					weiboUser.setCreateTime(rs.getTimestamp("CREATE_TIME"));
					weiboUser.setVerifyInfo(rs.getString("VERIFY_INFO"));
					weiboUser.setDaren(rs.getString("DAREN"));
					weiboUser.setBirthday(rs.getString("BIRTHDAY"));
					weiboUser.setQq(rs.getString("QQ"));
					weiboUser.setMsn(rs.getString("MSN"));
					weiboUser.setEmail(rs.getString("EMAIL"));
					weiboUser.setBlog(rs.getString("BLOG"));
					weiboUser.setDomain(rs.getString("DOMAIN"));
					weiboUser.setRegion(rs.getString("REGION"));
					weiboUser.setFollowUserId(rs.getString("FOLLOW_USERID"));
					weiboUser.setuCreateTime(rs.getTimestamp("U_CREATE_TIME"));
					weiboUser.setUpdateTime(rs.getTimestamp("UPDATE_TIME"));
					weiboUser.setFansUserId(rs.getString("FANS_USERID"));
					weiboUser.setDengji(rs.getString("DENGJI"));
					weiboUser.setMsgExistent(rs.getString("MSG_EXISTENT"));
					return weiboUser;
				}
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error(e);
			} finally {
				try {
					rs.close();
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	/**
	 * 更新微博用户信息到微博用户表，并将更新结果返回用户列队
	 * 
	 * @param weiboUser
	 * @return
	 */
	public synchronized int upDateWeiboUser(WeiboUser weiboUser) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int flag = 0;
		String userId = weiboUser.getUserId();
		if (userId == null)
			return 0;
		String screenName = weiboUser.getScreenName();
		String sex = weiboUser.getSex();
		String description = weiboUser.getDescription();
		String userName = weiboUser.getUserName();
		int followNum = weiboUser.getFollowNum();
		int fansNum = weiboUser.getFansNum();
		int messageNum = weiboUser.getMessageNum();
		String profileImageUrl = weiboUser.getProfileImageUrl();
		String isVerified = weiboUser.getIsVerified();
		String careerInfo = weiboUser.getCareerInfo();
		String educationInfo = weiboUser.getEducationInfo();
		String tag = weiboUser.getTag();
		String verifyInfo = weiboUser.getVerifyInfo();
		String daren = weiboUser.getDaren();
		String birthday = weiboUser.getBirthday();
		String qq = weiboUser.getQq();
		String msn = weiboUser.getMsn();
		String email = weiboUser.getEmail();
		String blog = weiboUser.getBlog();
		String domain = weiboUser.getDomain();
		String dengji = weiboUser.getDengji();
		String vip = weiboUser.getVip();
		String region = weiboUser.getRegion();
		String followUserId = weiboUser.getFollowUserId();
		String fansUserId = weiboUser.getFansUserId();
		Timestamp uCreateTime = null;
		if (weiboUser.getuCreateTime() != null) {
			uCreateTime = new Timestamp(weiboUser.getuCreateTime().getTime());
		}
		String msgExistent;
		if (weiboUser.getMsgExistent() == null) {
			msgExistent = "0";
		} else {
			msgExistent = "1";
		}
		Date updateTime = new Date();
		String sql = "update T_WEIBO_CRAWLER_USER t set FANS_USERID=?,screen_name=?,sex=?,verify_info=?,description=?,region=?,username=?,follow_num=?,fans_num=?,message_num=?,career_info=?,education_info=?,profile_image_url=?,is_verified=?,tag=?,birthday=?,qq=?,msn=?,email=?,u_create_time=?,follow_userid=?,vip=?,daren=?,dengji=?,update_time=?,blog=?,domain=?,msg_existent=? where userid=? ";
		try {
			pstmt = oconn.prepareStatement(sql);
			pstmt.setClob(1, strtoColb(fansUserId));
			pstmt.setString(2, screenName);
			pstmt.setString(3, sex);
			pstmt.setString(4, verifyInfo);
			pstmt.setString(5, description);
			pstmt.setString(6, region);
			pstmt.setString(7, userName);
			pstmt.setInt(8, followNum);
			pstmt.setInt(9, fansNum);
			pstmt.setInt(10, messageNum);
			pstmt.setString(11, careerInfo);
			pstmt.setString(12, educationInfo);
			pstmt.setString(13, profileImageUrl);
			pstmt.setString(14, isVerified);
			pstmt.setString(15, tag);
			pstmt.setString(16, birthday);
			pstmt.setString(17, qq);
			pstmt.setString(18, msn);
			pstmt.setString(19, email);
			pstmt.setTimestamp(20, uCreateTime);
			pstmt.setClob(21, strtoColb(followUserId));
			pstmt.setString(22, vip);
			pstmt.setString(23, daren);
			pstmt.setString(24, dengji);
			pstmt.setTimestamp(25, new Timestamp(updateTime.getTime()));
			pstmt.setString(26, blog);
			pstmt.setString(27, domain);
			pstmt.setString(28, msgExistent);
			pstmt.setString(29, userId);
			flag = pstmt.executeUpdate();
		} catch (SQLException e1) {
			e1.printStackTrace();
			logger.error(e1);
		} finally {
			try {
				pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (flag > 0) {
			sql = "select * from T_WEIBO_CRAWLER_QUEUE t where t.userid='"
					+ userId + "'";
			int updateNum = 0;
			try {
				pstmt = oconn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					updateNum = rs.getInt("UPDATE_NUM");
				}
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error(e);
			} finally {
				try {
					rs.close();
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			sql = "update T_WEIBO_CRAWLER_QUEUE t set t.is_success='0',t.update_num=?,t.update_time=? where t.userid='"
					+ userId + "'";
			try {
				pstmt = oconn.prepareStatement(sql);
				pstmt.setInt(1, updateNum + 1);
				pstmt.setTimestamp(2, new Timestamp(new Date().getTime()));
				pstmt.executeUpdate();
				pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error(e);
			} finally {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			return 1;
		}
		return 0;
	}

	/**
	 * 插入微博用户信息到微博用户表，并将插入结果返回用户列队
	 * 
	 * @param weiboUser
	 */
	public synchronized int inSertWeiboUser(WeiboUser weiboUser) {
		PreparedStatement pstmt = null;
		int flag = 0;
		String userId = weiboUser.getUserId();
		if (userId == null)
			return 0;
		String screenName = weiboUser.getScreenName();
		String sex = weiboUser.getSex();
		String description = weiboUser.getDescription();
		String userName = weiboUser.getUserName();
		int followNum = weiboUser.getFollowNum();
		int fansNum = weiboUser.getFansNum();
		int messageNum = weiboUser.getMessageNum();
		String profileImageUrl = weiboUser.getProfileImageUrl();
		String isVerified = weiboUser.getIsVerified();
		String careerInfo = weiboUser.getCareerInfo();
		String educationInfo = weiboUser.getEducationInfo();
		String tag = weiboUser.getTag();
		String verifyInfo = weiboUser.getVerifyInfo();
		String daren = weiboUser.getDaren();
		String birthday = weiboUser.getBirthday();
		String qq = weiboUser.getQq();
		String msn = weiboUser.getMsn();
		String email = weiboUser.getEmail();
		String blog = weiboUser.getBlog();
		String domain = weiboUser.getDomain();
		String dengji = weiboUser.getDengji();
		String vip = weiboUser.getVip();
		String region = weiboUser.getRegion();
		String followUserId = weiboUser.getFollowUserId();
		String fansUserId = weiboUser.getFansUserId();
		Timestamp uCreateTime = null;
		if (weiboUser.getuCreateTime() != null) {
			uCreateTime = new Timestamp(weiboUser.getuCreateTime().getTime());
		}
		Date updateTime = new Date();
		String sql = "INSERT INTO t_weibo_crawler_user (userid,screen_name,sex,verify_info,description,region,username,follow_num,fans_num,message_num,career_info,education_info,profile_image_url,is_verified,tag,birthday,qq,msn,email,u_create_time,follow_userid,vip,daren,dengji,fans_userid,create_time,update_time,blog,domain) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		try {
			pstmt = oconn.prepareStatement(sql);
			pstmt.setString(1, userId);
			pstmt.setString(2, screenName);
			pstmt.setString(3, sex);
			pstmt.setString(4, verifyInfo);
			pstmt.setString(5, description);
			pstmt.setString(6, region);
			pstmt.setString(7, userName);
			pstmt.setInt(8, followNum);
			pstmt.setInt(9, fansNum);
			pstmt.setInt(10, messageNum);
			pstmt.setString(11, careerInfo);
			pstmt.setString(12, educationInfo);
			pstmt.setString(13, profileImageUrl);
			pstmt.setString(14, isVerified);
			pstmt.setString(15, tag);
			pstmt.setString(16, birthday);
			pstmt.setString(17, qq);
			pstmt.setString(18, msn);
			pstmt.setString(19, email);
			pstmt.setTimestamp(20, uCreateTime);
			pstmt.setClob(21, strtoColb(followUserId));
			pstmt.setString(22, vip);
			pstmt.setString(23, daren);
			pstmt.setString(24, dengji);
			pstmt.setClob(25, strtoColb(fansUserId));
			pstmt.setTimestamp(26, new Timestamp(updateTime.getTime()));
			pstmt.setTimestamp(27, new Timestamp(updateTime.getTime()));
			pstmt.setString(28, blog);
			pstmt.setString(29, domain);
			flag = pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e);
		} finally {
			try {
				pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (flag > 0) {
			recoverUserIDfromQueue(userId);
			return 1;
		}
		return 0;
	}

	private CLOB strtoColb(String str) {
		if (str == null) {
			return null;
		}
		CLOB clob = null;
		try {
			clob = CLOB.createTemporary(oconn, false, CLOB.DURATION_SESSION);
			clob.open(CLOB.MODE_READWRITE);
			Writer writer = clob.getCharacterOutputStream();
			writer.write(str);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e);
		} catch (SQLException e1) {
			e1.printStackTrace();
			logger.error(e1);
		}
		return clob;
	}
}
