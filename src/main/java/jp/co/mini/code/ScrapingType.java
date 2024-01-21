package jp.co.mini.code;

import org.apache.commons.lang3.StringUtils;

/**
 * スクレイピング種別列挙
 */
public enum ScrapingType {

	GAME_RESULT("gameResult","試合結果"),
	TEAM_INFO("teamInfo","チーム情報");
	
	ScrapingType(String type, String name) {
		this.type = type;
		this.name = name;
	}
	
	private String type;
	private String name;
	
	public String getType() {
		return type;
	}
	public String getName() {
		return name;
	}
	
	public static ScrapingType getScrapingTypeByName (String name) {
		if (StringUtils.isEmpty(name)) {
			return null;
		}
		for (ScrapingType scrapingType : ScrapingType.values()) {
			if (StringUtils.equals(name, scrapingType.getName())) {
				return scrapingType;
			}
		}
		return null;
	}
	
	public static ScrapingType getScrapingTypeByType (String type) {
		if (StringUtils.isEmpty(type)) {
			return null;
		}
		for (ScrapingType scrapingType : ScrapingType.values()) {
			if (StringUtils.equals(type, scrapingType.getType())) {
				return scrapingType;
			}
		}
		return null;		
	}
	
}
