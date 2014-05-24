package crawler.weibo.service.filter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;

import utils.TextUtil;
import crawler.weibo.dao.UserJdbcService;
import crawler.weibo.model.WeiboUser;

public class WeiboUserFilter {

	private static final Log logger = LogFactory.getLog(WeiboUserFilter.class);
	/**
	 * 过滤规则列表
	 */
	private static ArrayList<UserFilterRule> ufrList = new ArrayList<UserFilterRule>();

	/**
	 * 根据规则，判断改用户是否继续爬下去，如果用户被过滤，返回true。
	 * 
	 * @param wu
	 * @return
	 */
	public static boolean filterUserByRules(WeiboUser wu) {
		for (int i = 0; i < ufrList.size(); i++) {
			UserFilterRule rule = ufrList.get(i);
			if (compareRule(rule, wu)) {
				logger.info("用户：" + wu.getScreenName() + "因为"
						+ rule.getColumnName() + " " + rule.getOperation()
						+ rule.getInputValue() + "而被过滤");
				return true;
			}
		}
		return false;
	}

	/**
	 * 规则对比,如果满足规则，返回true
	 * 
	 * @param rule
	 * @param wu
	 * @return
	 */
	private static boolean compareRule(UserFilterRule rule, WeiboUser wu) {
		String columnName = rule.getColumnName();
		if (rule.getInputType().equals("int")) {
			int columnValue = getIntColumnValueByName(columnName, wu);
			int ruleValue = Integer.valueOf(rule.getInputValue());
			String operation = rule.getOperation();
			if (operation.equals("=")) {
				if (columnValue == ruleValue) {
					return true;
				}
			} else if (operation.equals(">")) {
				if (columnValue > ruleValue) {
					return true;
				}
			} else if (operation.equals("<")) {
				if (columnValue < ruleValue) {
					return true;
				}
			} else {
				logger.error("出现未识别的操作符!" + operation);
			}
		} else if (rule.getInputType().equals("String")) {
			String columnValue = getStringColumnValueByName(columnName, wu);
			String ruleValue = rule.getInputValue();
			String operation = rule.getOperation();
			if (!operation.equals("like")) {
				logger.error("操作符出错，String操作法没有使用like关键字！");
				return true;
			}
			String matchMode = rule.getMatchMode();
			return textMatcher(ruleValue, columnValue, matchMode);
		} else if (rule.getInputType().equals("TimeStamp")) {// 第三种情况，为TimeStamp
			Timestamp columnValue = getTimeStampColumnValueByName(columnName,
					wu);
			Timestamp ruleValue = Timestamp.valueOf(rule.getInputValue()
					+ " 00:00:00");
			String operation = rule.getOperation();
			if (operation.equals("=")) {
				if (columnValue.equals(ruleValue)) {
					return true;
				}
			} else if (operation.equals(">")) {
				if (columnValue.after(ruleValue)) {
					return true;
				}
			} else if (operation.equals("<")) {
				if (columnValue.before(ruleValue)) {
					return true;
				}
			} else {
				logger.error("出现未识别的操作符!" + operation);
			}
		} else {
			logger.error("出现未识别的输入类型!" + rule.getInputType());
		}

		return false;
	}

	/**
	 * like的匹配模式: exact left_match right_match middle_match full_match
	 * 
	 * @param keyString
	 * @param text
	 * @param matchMode
	 * @return
	 */
	private static boolean textMatcher(String keyString, String text,
			String matchMode) {
		if (matchMode.equals("fuzzy")) {
			return TextUtil.fuzzyMatch(keyString, text);
		} else if (matchMode.equals("left")) {
			return TextUtil.leftMatch(keyString, text);
		} else if (matchMode.equals("right")) {
			return TextUtil.rightMatch(keyString, text);
		} else if (matchMode.equals("middle")) {
			return TextUtil.middleMatch(keyString, text);
		} else if (matchMode.equals("full")) {
			return TextUtil.fullMatch(keyString, text);
		} else {
			logger.error("匹配模式输入错误！");
		}
		return false;
	}

	/**
	 * 根据对象的String名称，从WeiboUser中用反射方法执行一个get方法获取改对象的值
	 * 
	 * @param rule
	 * @param wu
	 * @return
	 */
	private static int getIntColumnValueByName(String columnName, WeiboUser wu) {
		Class userClass = wu.getClass();
		String methodName = findMethodName(userClass, columnName);
		if (methodName == null) {
			logger.error(userClass.getName() + "中没有找到" + columnName + "的get方法");
		}
		int intValue = 0;
		try {
			intValue = (Integer) userClass.getMethod(methodName).invoke(wu);
		} catch (IllegalArgumentException e) {
			logger.error(e);
			e.printStackTrace();
		} catch (SecurityException e) {
			logger.error(e);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			logger.error(e);
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			logger.error(e);
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			logger.error(e);
			e.printStackTrace();
		}
		return intValue;
	}

	/**
	 * 根据对象的String名称，从WeiboUser中用反射方法执行一个get方法获取改对象的值
	 * 
	 * @param rule
	 * @param wu
	 * @return
	 */
	private static String getStringColumnValueByName(String columnName,
			WeiboUser wu) {
		Class userClass = wu.getClass();
		String methodName = findMethodName(userClass, columnName);
		if (methodName == null) {
			logger.error(userClass.getName() + "中没有找到" + columnName + "的get方法");
		}
		String strValue = "";
		try {
			strValue = (String) userClass.getMethod(methodName).invoke(wu);
		} catch (IllegalArgumentException e) {
			logger.error(e);
			e.printStackTrace();
		} catch (SecurityException e) {
			logger.error(e);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			logger.error(e);
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			logger.error(e);
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			logger.error(e);
			e.printStackTrace();
		}
		return strValue;
	}

	/**
	 * 根据对象的String名称，从WeiboUser中用反射方法执行一个get方法获取改对象的值
	 * 
	 * @param rule
	 * @param wu
	 * @return
	 */
	private static Timestamp getTimeStampColumnValueByName(String columnName,
			WeiboUser wu) {
		Class userClass = wu.getClass();
		String methodName = findMethodName(userClass, columnName);
		if (methodName == null) {
			logger.error(userClass.getName() + "中没有找到" + columnName + "的get方法");
		}
		Timestamp strValue = null;
		try {
			strValue = (Timestamp) userClass.getMethod(methodName).invoke(wu);
		} catch (IllegalArgumentException e) {
			logger.error(e);
			e.printStackTrace();
		} catch (SecurityException e) {
			logger.error(e);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			logger.error(e);
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			logger.error(e);
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			logger.error(e);
			e.printStackTrace();
		}
		return strValue;
	}

	/**
	 * 根据模型的对象名称，得到类中该对象的get方法名称
	 * 
	 * @param userClass
	 * @param columnName
	 * @return
	 */
	private static String findMethodName(Class userClass, String columnName) {
		String getMethodStr = "get" + columnName;
		Method[] methods = userClass.getMethods();
		for (int i = 0; i < methods.length; i++) {
			String methodStr = methods[i].getName();
			if (methodStr.equalsIgnoreCase(getMethodStr)) {
				return methodStr;
			}
		}
		return null;
	}

	/**
	 * 从json数据中读入过滤规则入规则列表
	 * 
	 * @param jsonArray
	 * @return
	 */
	public static int reflashUfrList(JSONArray jsonArray) {
		ufrList.clear();
		int size = jsonArray.length();
		for (int i = 0; i < size; i++) {
			UserFilterRule rule = new UserFilterRule();
			try {
				rule.setColumnName(jsonArray.getJSONObject(i).getString(
						"columnName"));
				rule.setComments(jsonArray.getJSONObject(i).getString(
						"comments"));
				rule.setInputValue(jsonArray.getJSONObject(i).getString(
						"inputValue"));
				rule.setInputType(jsonArray.getJSONObject(i).getString(
						"inputType"));
				rule.setOperation(jsonArray.getJSONObject(i).getString(
						"operation"));
				rule.setMatchMode(jsonArray.getJSONObject(i).getString(
						"matchMode"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (ufrList.add(rule)) {
				logger.info("成功添加规则" + rule);
			} else {
				logger.info("规则添加失败!" + rule);
			}
		}
		return size;
	}

	/**
	 * 根据存在数据库中的过滤用户表，如果用户被过滤，返回true。
	 * 
	 * @param wu
	 * @return
	 */
	public static boolean filterUserByFilUserTab(String userId) {
		if (UserJdbcService.getInstance().checkUserFilterList(userId)) {
			logger.info(userId + "已经在过滤表中存在..");
			return true;
		}
		return false;
	}

	/**
	 * 根据存在数据库中的用户表，如果用户存在，返回true。
	 * 
	 * @param wu
	 * @return
	 */
	public static boolean filterUserByUserTab(String userId) {
		if ((UserJdbcService.getInstance().getWeiboUser(userId)) != null) {
			logger.info(userId + "已经在用户表中存在..");
			return true;
		}
		return false;
	}

}
