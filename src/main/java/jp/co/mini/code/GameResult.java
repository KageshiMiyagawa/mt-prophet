package jp.co.mini.code;

import org.apache.commons.lang3.StringUtils;

/**
 * 試合結果列挙
 */
public enum GameResult {

	BEFORE("0","試合前"),
	HOME_WIN("1","ホーム勝ち"),
	AWAY_WIN("2","アウェイ勝ち"),
	DRAW("3","引き分け");
	
	GameResult(String code, String name) {
		this.code = code;
		this.name = name;
	}
	
	private String code;
	private String name;
	
	public String getCode() {
		return code;
	}
	public String getName() {
		return name;
	}
	
	public static GameResult getGameResultByName (String name) {
		if (StringUtils.isEmpty(name)) {
			return null;
		}
		for (GameResult gameResult : GameResult.values()) {
			if (StringUtils.equals(name, gameResult.getName())) {
				return gameResult;
			}
		}
		return null;
	}
	
	public static GameResult getGameResultByCode (String code) {
		if (StringUtils.isEmpty(code)) {
			return null;
		}
		for (GameResult gameResult : GameResult.values()) {
			if (StringUtils.equals(code, gameResult.getCode())) {
				return gameResult;
			}
		}
		return null;		
	}
	
}
