package utils;

public class TextUtil {

	public static boolean exactMatch(String keyString, String text) {
		if (text.indexOf(keyString) != -1) {
			return true;
		}
		return false;
	}

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

	public static boolean rightMatch(String keyString, String text) {
		return false;
	}

	public static boolean middleMatch(String keyString, String text) {
		return false;
	}

	public static boolean fullMatch(String keyString, String text) {
		return false;
	}
}
