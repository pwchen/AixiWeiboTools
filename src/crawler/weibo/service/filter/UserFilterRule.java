package crawler.weibo.service.filter;

import java.util.LinkedHashMap;
import java.util.Map;

public class UserFilterRule {
	public static String MATCH_MODE_FUZZY = "fuzzy";// 模糊匹配，包含左匹配、右匹配、中间匹配和全匹配的结果
	public static String MATCH_MODE_LEFT = "left";// 左匹配，从左边第一个字符匹配
	public static String MATCH_MODE_RIGHT = "right";// 右匹配，从右边第一个字符匹配
	public static String MATCH_MODE_MIDDLE = "middle";// 中间匹配，去除左右第一个字符进行精确匹配
	public static String MATCH_MODE_FULL = "full";// 全匹配，从第一个字符开始，一直匹配至最后一个字符
	public static Map<String, String> MATCHMODE_MAP = new LinkedHashMap<String, String>();

	public static String OPERATION_EQUAL = "=";
	public static String OPERATION_LIKE = "like";
	public static String OPERATION_LESSTHAN = "<";
	public static String OPERATION_LARGETHAN = ">";
	public static Map<String, String> OPERATION_MAP = new LinkedHashMap<String, String>();

	/**
	 * 字段名
	 */
	private String columnName;
	/**
	 * 字段描述
	 */
	private String label;
	/**
	 * 过滤值
	 */
	private String inputValue;
	/**
	 * 数据类型：String;int;Timestamp
	 */
	private String inputType;
	/**
	 * 查询过滤类型 = like > <
	 */
	private String operation;
	/**
	 * like的匹配模式: exact left_match right_match middle_match full_match
	 */
	private String matchMode;
	/***************** 字典 *********************/
	private String defaultMatchMode; // 匹配模式:exact;left_match;right_match;middle_match;full_text
	private String defaultOperation; // 查询操作:＝;like;<;>

	static {
		MATCHMODE_MAP.put(MATCH_MODE_FUZZY, "精确匹配");
		MATCHMODE_MAP.put(MATCH_MODE_LEFT, "左匹配");
		MATCHMODE_MAP.put(MATCH_MODE_RIGHT, "右匹配");
		MATCHMODE_MAP.put(MATCH_MODE_MIDDLE, "中间匹配");
		MATCHMODE_MAP.put(MATCH_MODE_FULL, "全文匹配");

		OPERATION_MAP.put(OPERATION_EQUAL, "等于");
		OPERATION_MAP.put(OPERATION_LIKE, "匹配");
		OPERATION_MAP.put(OPERATION_LESSTHAN, "小于");
		OPERATION_MAP.put(OPERATION_LARGETHAN, "大于");
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * String;int;Timestamp
	 * 
	 * @return
	 */
	public String getInputType() {
		return inputType;
	}

	public void setInputType(String inputType) {
		this.inputType = inputType;
	}

	public String getDefaultMatchMode() {
		return defaultMatchMode;
	}

	public void setDefaultMatchMode(String defaultMatchMode) {
		this.defaultMatchMode = defaultMatchMode;
	}

	public String getDefaultOperation() {
		return defaultOperation;
	}

	public void setDefaultOperation(String defaultOperation) {
		this.defaultOperation = defaultOperation;
	}

	public String getInputValue() {
		return inputValue;
	}

	public void setInputValue(String inputValue) {
		this.inputValue = inputValue;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getMatchMode() {
		return matchMode;
	}

	public void setMatchMode(String matchMode) {
		this.matchMode = matchMode;
	}
	
	
}
