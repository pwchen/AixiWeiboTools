package crawler.weibo.service.scheduler;

public class Task {
	private String userId;
	/**
	 * 0 爬取用户基本信息，以及用户的粉丝和关注信息 1 爬某一用户的所有微博信息 2 爬某条微博的所有转发评论信息
	 */
	int type;
	int depth;

	public Task(String userId, int type, int depth) {
		this.userId = userId;
		this.type = type;
		this.depth = depth;
	}

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
	
	/**
	 * 重写的equals方法，用户判断两个任务是否相同，只要ID和任务类型相同，则任务相同
	 */
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
		return "用户Id:" + this.userId + "，任务类型:" + this.type + ",深度："
				+ this.depth + " ";
	}

}
