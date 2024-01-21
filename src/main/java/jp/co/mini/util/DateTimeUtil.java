package jp.co.mini.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 日時関連のユーティリティクラス
 */
public class DateTimeUtil {

	/**
	 * 現在日時を取得して、指定のフォーマットに変換した文字列を応答する。
	 * 
	 * @param format フォーマット
	 * @return 現在日時文字列
	 */
	public static String getNowDateStr(String format) {
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
		return now.format(formatter);
	}

	/**
	 * 指定した日時の文字列を、指定したフォーマットに変換した文字列にして応答する。
	 * 
	 * @param inputDateStr 入力日時
	 * @param inputFormat 入力日時のフォーマット
	 * @param outputFormat 出力日時のフォーマット
	 * @return 指定のフォーマットへ変換した日時の文字列
	 */
	public static String convertDateStrFormat(String inputDateStr, String inputFormat, String outputFormat) {
		String convertDataStr;
		SimpleDateFormat inputDateFormat = new SimpleDateFormat(inputFormat);
		SimpleDateFormat outputDateFormat = new SimpleDateFormat(outputFormat);
		try {
			Date inputDate = inputDateFormat.parse(inputDateStr);
			convertDataStr = outputDateFormat.format(inputDate);
			return convertDataStr;
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 現在年を取得する
	 * 
	 * @return 現在年
	 */
	public static int getCurrentYear() {
		LocalDate currentDate = LocalDate.now();
		int year = currentDate.getYear();
		
		return year;
	}

	/**
	 * 現在年から過去X年を取得して応答する
	 * 
	 * @param count 取得する件数
	 * @return 現在年から過去X年を取得したリスト
	 */
	public static List<Integer> getPastYears(int count) {
		List<Integer> pastYears = new ArrayList<>();

		Calendar calendar = Calendar.getInstance();
		int currentYear = calendar.get(Calendar.YEAR);

		// 現在の年を含めて、直近のcount個の年を生成する
		for (int i = count - 1; i >= 0; i--) {
			int year = currentYear - i;
			pastYears.add(year);
		}

		return pastYears;
	}
	
}
