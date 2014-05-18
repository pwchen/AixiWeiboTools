package crawler.weibo.service.scheduler;

public class Task {
	private String userId;
	/**
	 * 0 爬取用户基本信息，以及用户的粉丝和关注信息，粉丝和关注不再加入任务队列; 
	 * 1 爬取用户基本信息，以及用户的粉丝和关注信息，并将粉丝和关注加入任务队列
	 */
	int type;
	int depth;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	
	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	@Override
	public boolean equals(Object obj) {
		Task task = (Task) obj;
		if (task != null && (task.getUserId()).equals(this.getUserId())
				&& (task.getType() == this.getType()))
			return true;
		return false;
	}

	@Override
	public String toString() {
		return "用户Id:" + this.userId + "，任务类型:" + this.type + ",深度："+this.depth+" ";
	}

}
