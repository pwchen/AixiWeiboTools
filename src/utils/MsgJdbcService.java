package utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import crawler.weibo.dao.JDBCUtilSingle;
import crawler.weibo.model.WeiboMsg;

public class MsgJdbcService {
	private static final Connection oconn = JDBCUtilSingle.getInitJDBCUtil()
			.getConnection();
	private static final Log logger = LogFactory.getLog(MsgJdbcService.class);
	private static MsgJdbcService userJdbcService = null;

	public static MsgJdbcService getInstance() {
		if (userJdbcService == null) {
			synchronized (MsgJdbcService.class) {
				if (userJdbcService == null) {
					userJdbcService = new MsgJdbcService();
				}
			}
		}
		return userJdbcService;
	}

	public int saveMsg(WeiboMsg reMsg) {
		PreparedStatement pstmt = null;
		String mId = reMsg.getId();
		if (getMsgByMid(mId) != null) {
			logger.info(mId + "在数据库中已经存在!");
			// System.out.println(Thread.currentThread().getName()
			// + "存入微博消息失败，MID：" + mId + "在数据库中已经存在!");
			return 0;
		}
		if (mId == null) {
			return 0;
		}
		String screenName = reMsg.getScreenName();
		String userName = reMsg.getUserName();
		String userId = reMsg.getUserId();
		String retweetedId = reMsg.getRetweetedId();
		String atUsers = reMsg.getAtUsers();
		String content = reMsg.getContent();
		String contentUrl = reMsg.getContentUrl();
		String source = reMsg.getSource();
		String pictureUrl = reMsg.getPictureUrl();
		String voiceUrl = reMsg.getVoiceUrl();
		String videoUrl = reMsg.getVideoUrl();
		int likeNum = reMsg.getLikeNum();
		int retweetNum = reMsg.getRetweetNum();
		int commentNum = reMsg.getCommentNum();
		Date publicTime = reMsg.getPublicTime();
		Date createTime = reMsg.getCreateTime();
		Date updateTime = reMsg.getUpdateTime();
		String gongyiUrl = reMsg.getGongyiUrl();

		String sql = "INSERT INTO t_weibo_message_test (id,user_id,user_name,screen_name,retweet_id,content,content_url,source,picture_url,voice_url, video_url, retweet_num, comment_num, public_time, at_users,create_time, update_time, like_num,gongyi_url) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		try {
			pstmt = oconn.prepareStatement(sql);
			pstmt.setString(1, mId);
			pstmt.setString(2, userId);
			pstmt.setString(3, userName);
			pstmt.setString(4, screenName);
			pstmt.setString(5, retweetedId);
			pstmt.setString(6, content);
			pstmt.setString(7, contentUrl);
			pstmt.setString(8, source);
			pstmt.setString(9, pictureUrl);
			pstmt.setString(10, voiceUrl);
			pstmt.setString(11, videoUrl);
			pstmt.setInt(12, retweetNum);
			pstmt.setInt(13, commentNum);
			pstmt.setTimestamp(14, new java.sql.Timestamp(publicTime.getTime()));
			pstmt.setString(15, atUsers);
			pstmt.setTimestamp(16, new java.sql.Timestamp(createTime.getTime()));
			pstmt.setTimestamp(17, new java.sql.Timestamp(updateTime.getTime()));
			pstmt.setInt(18, likeNum);
			pstmt.setString(19, gongyiUrl);
			return pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e);
		} finally {
			try {
				pstmt.close();
			} catch (SQLException e) {
				logger.error(e);
				e.printStackTrace();
			}
		}
		return 0;
	}

	private Object getMsgByMid(String mId) {
		String sql = "select * from T_WEIBO_MESSAGE_TEST t where t.id=?";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = oconn.prepareStatement(sql);
			pstmt.setString(1, mId);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				return 1;
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

	public int getMsgCountById(String userId) {
		String sql = "select count(*) as count from T_WEIBO_MESSAGE_TEST t where t.user_id=?";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = oconn.prepareStatement(sql);
			pstmt.setString(1, userId);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getInt("count");
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
		return 0;
	}

	/**
	 * 获取userId 转发followUserId 微博的数量
	 * 
	 * @param userId
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public int getMsgsNumbyUid(String userId, String followUserId) {
		String sql = "select count(*) from t_weibo_message_test t where t.user_id=? and t.id in (select t1.retweet_id from T_WEIBO_MESSAGE_TEST t1 where t1.user_id=?)";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = oconn.prepareStatement(sql);
			pstmt.setString(1, followUserId);
			pstmt.setString(2, userId);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getInt("count(*)");
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
		return 0;
	}
}
