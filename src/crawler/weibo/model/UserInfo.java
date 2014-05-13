package crawler.weibo.model;

public class UserInfo {
    /**
     * 通过名字可以构造用户首页url
     */
    private String nickName;//有nick name的用户才是真正存在的
    private String userId;
    private String homeUrl;//通常是/u/userid
    public String getNickName() {
        return nickName;
    }
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getHomeUrl() {
        return homeUrl;
    }
    public void setHomeUrl(String homeUrl) {
        this.homeUrl = homeUrl;
    }
    
    
}
