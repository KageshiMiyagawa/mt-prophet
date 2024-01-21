package jp.co.mini.model;

import lombok.Data;

/**
 * チーム情報
 */
@Data
public class ClubTeamInfo {

	private String teamName;
	
	private String teamShortName;
	
	private String league;
	
	private Integer rate;
	
	private String teamLink;
	
}
