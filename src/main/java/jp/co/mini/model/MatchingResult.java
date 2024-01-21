package jp.co.mini.model;

import java.util.List;

import lombok.Data;

/**
 * 予測結果情報
 */
@Data
public class MatchingResult {

	private int prophetNo;
	private String type;
	private String typeStyle;
	private List<MatchingGameInfo> matchingGameInfo;
	private String probability;
	private String probabilityStyle;
	
	@Data
	public static class MatchingGameInfo {
		private String date;
		private String home;
		private String away;
		private String prophetInfo;
		private double prophetPercent;
		private String prophetInfoStyle;
		private String winPercent;
		private String losePercent;
		private String drawPercent;
		private Integer homeRate;
		private Integer awayRate;
	}

}
