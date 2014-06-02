package crawler.weibo.model;

import java.sql.Timestamp;
import java.util.ArrayList;

public class WeiboUser {
	private String userId;
	private String screenName;
	private String sex;
	private String description;
	private String userName;
	private int followNum;
	private int fansNum;
	private int messageNum;
	private String profileImageUrl;
	private String isVerified;
	private String careerInfo;
	private String educationInfo;
	private String tag;
	private String daren;
	private String birthday;
	private String qq;
	private String msn;
	private String email;
	private String dengji;
	private String vip;
	private String region;
	private String followUserId;
	private String fansUserId;
	private String verifyInfo;
	private String blog;
	private String domain;
	private Timestamp uCreateTime;
	private Timestamp createTime;
	private Timestamp updateTime;
	private String msgExistent;

	public String getMsgExistent() {
		return msgExistent;
	}

	public void setMsgExistent(String msgExistent) {
		this.msgExistent = msgExistent;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getScreenName() {
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public int getFollowNum() {
		return followNum;
	}

	public void setFollowNum(int followNum) {
		this.followNum = followNum;
	}

	public int getFansNum() {
		return fansNum;
	}

	public void setFansNum(int fansNum) {
		this.fansNum = fansNum;
	}

	public int getMessageNum() {
		return messageNum;
	}

	public void setMessageNum(int messageNum) {
		this.messageNum = messageNum;
	}

	public String getProfileImageUrl() {
		return profileImageUrl;
	}

	public void setProfileImageUrl(String profileImageUrl) {
		this.profileImageUrl = profileImageUrl;
	}

	public String getIsVerified() {
		return isVerified;
	}

	public void setIsVerified(String isVerified) {
		this.isVerified = isVerified;
	}

	public String getCareerInfo() {
		return careerInfo;
	}

	public void setCareerInfo(String careerInfo) {
		this.careerInfo = careerInfo;
	}

	public String getEducationInfo() {
		return educationInfo;
	}

	public void setEducationInfo(String educationInfo) {
		this.educationInfo = educationInfo;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getDaren() {
		return daren;
	}

	public void setDaren(String daren) {
		this.daren = daren;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public String getQq() {
		return qq;
	}

	public void setQq(String qq) {
		this.qq = qq;
	}

	public String getMsn() {
		return msn;
	}

	public void setMsn(String msn) {
		this.msn = msn;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getVerifyInfo() {
		return verifyInfo;
	}

	public void setVerifyInfo(String verifyInfo) {
		this.verifyInfo = verifyInfo;
	}

	public String getBlog() {
		return blog;
	}

	public void setBlog(String blog) {
		this.blog = blog;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getDengji() {
		return dengji;
	}

	public void setDengji(String dengji) {
		this.dengji = dengji;
	}

	public String getVip() {
		return vip;
	}

	public void setVip(String vip) {
		this.vip = vip;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getFollowUserId() {
		return followUserId;
	}

	public void setFollowUserId(String followUserId) {
		this.followUserId = followUserId;
	}

	public String getFansUserId() {
		return fansUserId;
	}

	public void setFansUserId(String fansUserId) {
		this.fansUserId = fansUserId;
	}

	public Timestamp getuCreateTime() {
		return uCreateTime;
	}

	public void setuCreateTime(Timestamp uCreateTime) {
		this.uCreateTime = uCreateTime;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	/**
	 * 生成粉丝列表
	 * 
	 * @return
	 */
	public String[] generateFansArray() {
		if (fansUserId == null) {
			return null;
		} else {
			return fansUserId.split(",");
		}
	}

	/**
	 * 生成关注列表
	 * 
	 * @return
	 */
	public String[] generateFollowsArray() {
		if (followUserId == null) {
			return null;
		} else {
			return followUserId.split(",");
		}
	}

	/**
	 * 生成关注+粉丝列表
	 * 
	 * @return
	 */
	public String[] generateRelationArray() {
		String[] userArr1 = generateFansArray();
		String[] userArr2 = generateFollowsArray();
		if (userArr1 == null) {
			if (userArr2 == null) {
				return null;
			}
			return userArr2;
		} else if (userArr2 == null) {
			return userArr1;
		}
		int userArr1Len = userArr1.length;
		ArrayList<String> userList = new ArrayList<String>();
		for (int i = 0; i < userArr1Len; i++)
			userList.add(userArr1[i]);
		for (int i = 0; i < userArr2.length; i++) {// 去重
			boolean flag = true;
			for (int j = 0; j < userArr1Len; j++) {
				if (userArr1[j] == userArr2[i]) {
					flag = false;
					break;
				}
			}
			if (flag) {
				userList.add(userArr2[i]);
			}
		}
		return userList.toArray(userArr1);
	}

	public String toString() {
		String str = "userId-" + userId + "\n screenName-" + screenName
				+ "\n sex-" + sex + "\n description-" + description
				+ "\n userName-" + userName + "\n followNum-" + followNum
				+ "\n fansNum-" + fansNum + "\n messageNum-" + messageNum
				+ "\n profileImageUrl-" + profileImageUrl + "\n isVerified-"
				+ isVerified + "\n careerInfo-" + careerInfo
				+ "\n educationInfo-" + educationInfo + "\n tag-" + tag
				+ "\n daren-" + daren + "\n birthday-" + birthday + "\n qq-"
				+ qq + "\n msn-" + msn + "\n email-" + email + "\n dengji-"
				+ dengji + "\n vip-" + vip + "\n region-" + region
				+ "\n followUserId-" + followUserId + "\n fansUserId-"
				+ fansUserId + "\n verifyInfo-" + verifyInfo + "\n blog-"
				+ blog + "\n domain-" + domain + "\n uCreateTime-"
				+ uCreateTime + "\n createTime-" + createTime
				+ "\n updateTime-" + updateTime;
		return str;
	}
}
