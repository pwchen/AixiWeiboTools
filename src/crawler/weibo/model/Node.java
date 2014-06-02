package crawler.weibo.model;

public class Node {
	private int id;// 节点ID
	private long userId;// 该节点的用户Id
	private int fans;// 粉丝数
	private int follow;// 关注数
	private int degree;// 该用户在网络中的出度加入度
	private int outDegree;// 该用户在网络中的出度
	private double bc;// Betweenness Centrality
	private double cc;// Closeness Centrality
	private int kc;// K-Croe

	private double tc;// 传播能力,初始化的时候根据度设定
	private int it;// 已感染持续能力
	private double ic;// 免疫能力，初始话的时候根据度设定，与tc等值
	private int s;// 节点状态 0为待感染 1为已接触 2为已感染节点 3为免疫节点
	private double ii;// 被感染的强度

	/**
	 * 以该节点为初始传染节点，收敛的迭代次数，maxIS，RS的数量比例
	 */
	private double itNum;// 收敛迭代次数
	private double maxIS;// 最大的IS占比
	private double RS;// RS占比

	/**
	 * 根据属性名称获取该属性值，统一用double返回
	 * 
	 * @param name
	 * @return
	 */
	public double getPropertyByString(String name) {
		if (name.equalsIgnoreCase("fans")) {
			return (double) fans;
		} else if (name.equalsIgnoreCase("degree")) {
			return (double) degree;
		} else if (name.equalsIgnoreCase("outDegree")) {
			return (double) outDegree;
		} else if (name.equalsIgnoreCase("bc")) {
			return bc;
		} else if (name.equalsIgnoreCase("cc")) {
			return cc;
		} else if (name.equalsIgnoreCase("kc")) {
			return (double) kc;
		} else {
			return -1;
		}
	}

	public void initCap() {
		this.setIi(0);
		this.setS(0);
		this.setIt(0);
	}

	public Node(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public int getFans() {
		return fans;
	}

	public void setFans(int fans) {
		this.fans = fans;
	}

	public int getFollow() {
		return follow;
	}

	public void setFollow(int follow) {
		this.follow = follow;
	}

	public int getDegree() {
		return degree;
	}

	public void setDegree(int degree) {
		this.degree = degree;
	}

	public int getOutDegree() {
		return outDegree;
	}

	public void setOutDegree(int outDegree) {
		this.outDegree = outDegree;
	}

	public double getBc() {
		return bc;
	}

	public void setBc(double bc) {
		this.bc = bc;
	}

	public double getCc() {
		return cc;
	}

	public void setCc(double cc) {
		this.cc = cc;
	}

	public int getKc() {
		return kc;
	}

	public void setKc(int kc) {
		this.kc = kc;
	}

	public double getTc() {
		return tc;
	}

	public void setTc(double tc) {
		this.tc = tc;
	}

	public int getIt() {
		return it;
	}

	public void setIt(int it) {
		this.it = it;
	}

	public double getIc() {
		return ic;
	}

	public void setIc(double ic) {
		this.ic = ic;
	}

	public int getS() {
		return s;
	}

	public void setS(int s) {
		this.s = s;
	}

	public double getIi() {
		return ii;
	}

	public void setIi(double ii) {
		this.ii = ii;
	}

	public String getNodeWriteString() {
		return userId + "," + fans + "," + follow + "," + degree + ","
				+ outDegree + "," + bc + "," + cc + "," + kc + "\r\n";
	}

	public double getItNum() {
		return itNum;
	}

	public void setItNum(double itNum) {
		this.itNum = itNum;
	}

	public double getMaxIS() {
		return maxIS;
	}

	public void setMaxIS(double maxIS) {
		this.maxIS = maxIS;
	}

	public double getRS() {
		return RS;
	}

	public void setRS(double rS) {
		RS = rS;
	}

	@Override
	public String toString() {
		String sstr = "";
		if (s == 0) {
			sstr = "待接触";
		} else if (s == 1) {
			sstr = "已接触";
		} else if (s == 2) {
			sstr = "已感染";
		} else {
			sstr = "免疫";
		}
		return "【node】ID:" + id + " 出度:" + outDegree + " 传播能力:"
				+ String.format("%.2f", tc) + " 免疫能力:"
				+ String.format("%.2f", ic) + " 被感染程度："
				+ String.format("%.2f", ii) + " 状态：" + sstr;
	}
}
