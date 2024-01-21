package jp.co.mini.model;

import java.util.List;

import jp.co.mini.model.table.TtGameResult;
import jp.co.mini.model.table.TtTeam;
import lombok.Data;

/**
 * 計算結果情報
 */
@Data
public class CalcRateResult {

	List<TtGameResult> gameResultList;
	
	List<TtTeam> clubTeamInfoList;
	
	List<String> unknownTeamNameList;

}
