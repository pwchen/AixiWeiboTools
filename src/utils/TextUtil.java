package utils;

public class TextUtil {

	/**
	 * 模糊匹配，只要文本中出现关键字，则返回true
	 * 
	 * @param keyString
	 * @param text
	 * @return
	 */
	public static boolean fuzzyMatch(String keyString, String text) {
		if (text.indexOf(keyString) != -1) {
			return true;
		}
		return false;
	}

	/**
	 * 左匹配，从文本第一个字符开始往后都与关键字对应字符相匹配，则返回true
	 * 
	 * @param keyString
	 * @param text
	 * @return
	 */
	public static boolean leftMatch(String keyString, String text) {
		char[] keycs = keyString.toCharArray();
		char[] textcs = text.toCharArray();
		for (int i = 0; i < keycs.length; i++) {
			if (keycs[i] != textcs[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 右匹配，从文本最后一个字符开始往前都与关键字字符相匹配，则返回true
	 * 
	 * @param keyString
	 * @param text
	 * @return
	 */
	public static boolean rightMatch(String keyString, String text) {
		char[] keycs = keyString.toCharArray();
		char[] textcs = text.toCharArray();
		int j = textcs.length - 1;
		for (int i = keycs.length - 1; i >= 0; i--, j--) {
			if (keycs[i] != textcs[j]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 中间匹配，去除文本字一个字符和最后一个字符，返回改文本的模糊匹配
	 * 
	 * @param keyString
	 * @param text
	 * @return
	 */
	public static boolean middleMatch(String keyString, String text) {
		String subText = text.substring(1, text.length() - 1);
		return fuzzyMatch(keyString, subText);
	}

	/**
	 * 全匹配,从第一个字符开始到最后一个字符，文本与关键字都相同，则返回true
	 * 
	 * @param keyString
	 * @param text
	 * @return
	 */
	public static boolean fullMatch(String keyString, String text) {
		char[] keycs = keyString.toCharArray();
		char[] textcs = text.toCharArray();
		int i = keycs.length;
		int j = textcs.length;
		if (i != j) {
			return false;
		}
		for (int k = 0; k < i; k++) {
			if (keycs[k] != textcs[k]) {
				return false;
			}
		}
		return true;
	}
}
