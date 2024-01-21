package jp.co.mini.model.table;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "tt_collect_manage")
public class TtCollectManage {
	
	@Id
	@Column(name = "collect_id")
	private int collectId;
	
	@Column(name = "collect_name")
	private String collectName;
	
	@Column(name = "last_collect_date")
	private String lastCollectDate;

}