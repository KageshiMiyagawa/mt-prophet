package jp.co.mini.model;

import lombok.Data;

/**
 * 試合結果情報
 */
@Data
public class GameResultData {

	private String year;
	
	private String tournament;
	
	private String section;
	
	private String gameDate;
	
	private String gameTime;
	
	private String home;
	
	private String score;
	
	private String away;
	
	private String stadium;
	
	private String result;
}
