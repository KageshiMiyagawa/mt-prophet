package jp.co.mini.util;

import java.text.Normalizer;

/**
 * 文字列変換ユーティリティ
 */
public class StringUtil {

	/**
	 * 半角文字列に変換する
	 * 
	 * @param input 入力文字列
	 * @return 半角文字列
	 */
	public static String toHalfWidth(String input) {
		String normalized = Normalizer.normalize(input, Normalizer.Form.NFKC);
		return normalized.replaceAll("[^\\x00-\\x7F]", "");
	}

	/**
	 * 全角文字列に変換する
	 * @param input 入力文字列
	 * @return 全角文字列
	 */
	public static String toFullWidth(String input) {
		String normalized = Normalizer.normalize(input, Normalizer.Form.NFKC);
		StringBuilder sb = new StringBuilder();
		for (char c : normalized.toCharArray()) {
			if (c >= 0x0021 && c <= 0x007E) {
				sb.append((char) (c + 0xFEE0));
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

}
