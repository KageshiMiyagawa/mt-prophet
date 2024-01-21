package jp.co.mini.model.table;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "tt_game_result")
public class TtGameResult {
	@Id
	@Column(name = "game_id")
	private int gameId;

	@Column(name = "year")
	private String year;

	@Column(name = "tournament")
	private String tournament;

	@Column(name = "section")
	private String section;

	@Column(name = "game_date")
	private String gameDate;

	@Column(name = "game_time")
	private String gameTime;

	@Column(name = "home")
	private String home;

	@Column(name = "score")
	private String score;
	
	@Column(name = "away")
	private String away;
	
	@Column(name = "stadium")
	private String stadium;

	@Column(name = "result")
	private String result;
	
	@Column(name = "home_rate")
	private Integer homeRate;

	@Column(name = "away_rate")
	private Integer awayRate;

	@Column(name = "calc_flag")
	private String calcFlag;

	@Column(name = "histogram_flag")
	private String histogramFlag;
	
	@Column(name = "regist_date")
	private String registDate;

	@Column(name = "update_date")
	private String updateDate;
}