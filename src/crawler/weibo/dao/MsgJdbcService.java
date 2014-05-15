package crawler.weibo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import crawler.weibo.model.WeiboMsg;

public class MsgJdbcService {
	private static final Connection oconn = JDBCUtilSingle.getInitJDBCUtil()
			.getConnection();
	private static final Log logger = LogFactory.getLog(MsgJdbcService.class);
	private static MsgJdbcService userJdbcService = null;

	/**
	 * 获取一个单例Service
	 * 
	 * @return
	 */
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

		String sql = "INSERT INTO t_weibo_message_test (id,user_id,user_name,screen_name,retweet_id,content,content_url,source,picture_url,voice_url, video_url, retweet_num, comment_num, public_time, at_users,create_time, update_time, like_num) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
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

	/**
	 * 传入userId和mId查询改条微博用户是否转发，如果转发，返回+1，否则-1
	 * 
	 * @param userId
	 * @param mId
	 * @return
	 */
	public String getLebel(String userId, String mId, WeiboMsg weiboMsg) {
		String retweetId = weiboMsg.getRetweetedId();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql1 = "select * from T_WEIBO_MESSAGE_TEST t where t.id=? and t.user_id=?";
		try {
			pstmt = oconn.prepareStatement(sql1);
			pstmt.setString(1, retweetId);
			pstmt.setString(2, userId);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				return "-1";
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
		String sql = "select * from T_WEIBO_MESSAGE_TEST t where t.user_id=? and (retweet_id=? or retweet_id=?) ";
		try {
			pstmt = oconn.prepareStatement(sql);
			pstmt.setString(1, userId);
			pstmt.setString(2, mId);
			pstmt.setString(3, retweetId);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				Date publicTime = rs.getTimestamp("public_time");
				if (publicTime.after(weiboMsg.getPublicTime())) {
					return "+1";
				}
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
		return "-1";
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

	/**
	 * 根据用户ID取出时间段微博列表，时间格式 yyyy-MM-dd
	 * 
	 * @param userId
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public List<WeiboMsg> getMsgsbyUid(String userId, String startDate,
			String endDate) {
		logger.info("开始获取用户ID为" + userId + "在" + startDate + "-" + endDate
				+ "的微博消息...");
		String sql = "select * from T_WEIBO_MESSAGE_TEST t where t.user_id = ? and t.public_time >= to_date(?,'yyyy-MM-dd') and t.public_time <= to_date(?,'yyyy-MM-dd') and t.retweet_num>100";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<WeiboMsg> weiboMsgList = new ArrayList<WeiboMsg>();
		try {
			pstmt = oconn.prepareStatement(sql);
			pstmt.setString(1, userId);
			pstmt.setString(2, startDate);
			pstmt.setString(3, endDate);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				weiboMsgList.add(feedMsg(rs));
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
		return weiboMsgList;
	}

	/**
	 * 获取时间段内所有微博列表，时间格式 yyyy-MM-dd
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public List<WeiboMsg> getAllMsgs(String startDate, String endDate) {
		logger.info("开始获取所有用户在" + startDate + "-" + endDate + "的微博消息...");
		String sql = "select * from T_WEIBO_MESSAGE_TEST t where t.public_time >= to_date(?,'yyyy-MM-dd') and t.public_time <= to_date(?,'yyyy-MM-dd') and rownum<30000 order by t.user_id ";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<WeiboMsg> weiboMsgList = new ArrayList<WeiboMsg>();
		try {
			pstmt = oconn.prepareStatement(sql);
			pstmt.setString(1, startDate);
			pstmt.setString(2, endDate);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				weiboMsgList.add(feedMsg(rs));
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
		return weiboMsgList;
	}

	private WeiboMsg feedMsg(ResultSet rs) {
		WeiboMsg weiboMsg = new WeiboMsg();
		try {
			weiboMsg.setId(rs.getString("id"));
			weiboMsg.setScreenName(rs.getString("screen_name"));
			weiboMsg.setUserName(rs.getString("user_name"));
			weiboMsg.setUserId(rs.getString("user_id"));
			weiboMsg.setRetweetedId(rs.getString("retweet_Id"));
			weiboMsg.setAtUsers(rs.getString("at_Users") == null ? "" : rs
					.getString("at_Users"));
			weiboMsg.setContent(rs.getString("content"));
			weiboMsg.setContentUrl(rs.getString("content_Url"));
			weiboMsg.setSource(rs.getString("source"));
			weiboMsg.setPictureUrl(rs.getString("picture_Url"));
			weiboMsg.setVoiceUrl(rs.getString("voice_Url"));
			weiboMsg.setVideoUrl(rs.getString("video_Url"));
			weiboMsg.setGongyiUrl(rs.getString("gongyi_Url"));
			weiboMsg.setLikeNum(rs.getInt("like_num"));
			weiboMsg.setRetweetNum(rs.getInt("retweet_num"));
			weiboMsg.setCommentNum(rs.getInt("comment_num"));
			weiboMsg.setPublicTime(rs.getTimestamp("public_time"));
			weiboMsg.setCreateTime(rs.getTimestamp("create_time"));
			weiboMsg.setUpdateTime(rs.getTimestamp("update_Time"));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return weiboMsg;
	}

	/**
	 * 查询userId转发过uId多少条微博
	 * 
	 * @param userId
	 * @param uId
	 * @return
	 */
	public int getReNum(String userId, String uId, String dateStr) {
		String sql = "select count(*) as count from T_WEIBO_MESSAGE_TEST t where t.user_id=? and t.public_time<=to_date(?,'yyyy-MM-dd') and retweet_id in (select t1.id from T_WEIBO_MESSAGE_TEST t1 where t1.user_id=?) ";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = oconn.prepareStatement(sql);
			pstmt.setString(1, userId);
			pstmt.setString(2, dateStr);
			pstmt.setString(3, uId);
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
}
