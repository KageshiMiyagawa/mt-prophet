package jp.co.mini.model.table;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "tt_histogram")
public class TtHistogram {
	@Id
	@Column(name = "record_number")
	private int recordNumber;

	@Column(name = "win_count")
	private int winCount;
	
	@Column(name = "lose_count")
	private int loseCount;

	@Column(name = "draw_count")
	private int drawCount;

	@Column(name = "rate_width")
	private int rateWidth;
	
	@Column(name = "description")
	private String description;

}