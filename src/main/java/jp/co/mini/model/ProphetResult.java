package jp.co.mini.model;

import lombok.Data;

/**
 * 予測結果情報
 */
@Data
public class ProphetResult {

	private String tournament;
	
	private String section;
	
	private String gameDate;
	
	private String home;

	private Integer homeRate;
	
	private String away;
	
	private Integer AwayRate;
	
	private String winP;
	
	private String loseP;
	
	private String drawP;
}
