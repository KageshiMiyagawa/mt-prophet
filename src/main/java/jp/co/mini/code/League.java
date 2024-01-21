package jp.co.mini.code;

import org.apache.commons.lang3.StringUtils;

/**
 * リーグ列挙
 */
public enum League {

	J1("Ｊ１"),
	J2("Ｊ２"),
	J3("Ｊ３");
	
	League(String name) {
		this.name = name;
	}
	
	private String name;
	
	public String getName() {
		return name;
	}
	
	public static League getLeagueByName (String name) {
		if (StringUtils.isEmpty(name)) {
			return null;
		}
		for (League league : League.values()) {
			if (StringUtils.equals(name, league.getName())) {
				return league;
			}
		}
		return null;
	}
	
}
