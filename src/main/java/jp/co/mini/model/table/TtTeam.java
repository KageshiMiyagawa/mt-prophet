package jp.co.mini.model.table;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "tt_team")
public class TtTeam {
	@Id
	@Column(name = "team_id")
	private int teamId;

	@Column(name = "team_name")
	private String teamName;
	
	@Column(name = "team_short_name")
	private String teamShortName;

	@Column(name = "league")
	private String league;

	@Column(name = "home_rate")
	private Integer homeRate;

	@Column(name = "away_rate")
	private Integer awayRate;
	
	@Column(name = "team_link")
	private String teamLink;

	@Column(name = "regist_date")
	private String registDate;

	@Column(name = "update_date")
	private String updateDate;

}