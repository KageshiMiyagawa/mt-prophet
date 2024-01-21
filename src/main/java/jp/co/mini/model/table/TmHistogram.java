package jp.co.mini.model.table;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tm_histogram")
public class TmHistogram {
	@Id
	@Column(name = "record_number")
	private int recordNumber;

	@Column(name = "win_percent")
	private double winPercent;
	
	@Column(name = "lose_percent")
	private double losePercent;

	@Column(name = "draw_percent")
	private double drawPercent;

	@Column(name = "rate_width")
	private int rateWidth;
	
	@Column(name = "description")
	private String description;

}