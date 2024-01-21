package jp.co.mini.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jp.co.mini.model.GameResultData;
import jp.co.mini.model.table.TtGameResult;

/**
 * 試合結果カスタムリポジトリ
 */
@Repository
public class GameResultCustomRepository {

	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private GameResultCrudRepository gameResultCrudRepository;

	/**
	 * 試合結果を登録する
	 * 
	 * @param scrapingGameResultList 試合結果リスト
	 * @param nowDt 現在日時
	 */
	public void saveGameResult(List<GameResultData> scrapingGameResultList, String nowDt) {
		List<TtGameResult> resultList = new ArrayList<>();
		int gameResultId = getMaxGameResultId();
		for(GameResultData scrapingGameResult : scrapingGameResultList) {
			TtGameResult result = new TtGameResult();
			BeanUtils.copyProperties(scrapingGameResult, result);
			result.setGameId(gameResultId);
			result.setCalcFlag("0");
			result.setHistogramFlag("0");
			result.setRegistDate(nowDt);
			result.setUpdateDate(nowDt);
			resultList.add(result);
			
			gameResultId ++;
		}
		
		gameResultCrudRepository.saveAll(resultList);
	}
	
	/**
	 * 登録する試合結果IDを取得する
	 * 
	 * @return 試合結果ID
	 */
	private int getMaxGameResultId() {
		Object maxGameResultId = entityManager.createNativeQuery("SELECT MAX(game_id) FROM tt_game_result;").getSingleResult();
		if (Objects.isNull(maxGameResultId)) {
			return 1;
		}
		return (int) maxGameResultId;
	}
}
