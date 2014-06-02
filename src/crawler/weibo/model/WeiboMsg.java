package crawler.weibo.model;

import java.util.Date;

public class WeiboMsg {

	private String id;
	private String userId;
	private String userName;
	private String screenName;
	private String retweetedId;
	private String atUsers;
	private String content;
	private String contentUrl;
	private String source;
	private String pictureUrl;
	private String voiceUrl;
	private String videoUrl;
	private String gongyiUrl;
	private int likeNum;
	private int retweetNum;
	private int commentNum;
	private Date publicTime;
	private Date createTime;
	private Date updateTime;

	public String getGongyiUrl() {
		return gongyiUrl;
	}

	public void setGongyiUrl(String gongyiUrl) {
		this.gongyiUrl = gongyiUrl;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getScreenName() {
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	public String getRetweetedId() {
		return retweetedId;
	}

	public void setRetweetedId(String retweetedId) {
		this.retweetedId = retweetedId;
	}

	public String getAtUsers() {
		return atUsers;
	}

	public void setAtUsers(String atUsers) {
		this.atUsers = atUsers;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getContentUrl() {
		return contentUrl;
	}

	public void setContentUrl(String contentUrl) {
		this.contentUrl = contentUrl;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getPictureUrl() {
		return pictureUrl;
	}

	public void setPictureUrl(String pictureUrl) {
		this.pictureUrl = pictureUrl;
	}

	public String getVoiceUrl() {
		return voiceUrl;
	}

	public void setVoiceUrl(String voiceUrl) {
		this.voiceUrl = voiceUrl;
	}

	public String getVideoUrl() {
		return videoUrl;
	}

	public void setVideoUrl(String videoUrl) {
		this.videoUrl = videoUrl;
	}

	public int getLikeNum() {
		return likeNum;
	}

	public void setLikeNum(int likeNum) {
		this.likeNum = likeNum;
	}

	public int getRetweetNum() {
		return retweetNum;
	}

	public void setRetweetNum(int retweetNum) {
		this.retweetNum = retweetNum;
	}

	public int getCommentNum() {
		return commentNum;
	}

	public void setCommentNum(int commentNum) {
		this.commentNum = commentNum;
	}

	public Date getPublicTime() {
		return publicTime;
	}

	public void setPublicTime(Date publicTime) {
		this.publicTime = publicTime;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	@Override
	public String toString() {
		String str = " id:" + id + " userId:" + userId + " userName:"
				+ userName + " screenName:" + screenName + " retweetedId:"
				+ retweetedId + " atUsers:" + atUsers + " content:" + content
				+ " contentUrl:" + contentUrl + " source:" + source
				+ " pictureUrl:" + pictureUrl + " voiceUrl:" + voiceUrl
				+ " videoUrl:" + videoUrl + " likeNum:" + likeNum
				+ " retweetNum:" + retweetNum + " commentNum: " + commentNum
				+ " publicTime: " + publicTime.toLocaleString()
				+ " createTime:" + createTime.toLocaleString() + " updateTime:"
				+ updateTime.toLocaleString();
		return str;
	}
}
