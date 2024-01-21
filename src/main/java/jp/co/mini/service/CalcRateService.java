package jp.co.mini.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import jp.co.mini.ApplicationConstants;
import jp.co.mini.code.GameResult;
import jp.co.mini.model.CalcRateResult;
import jp.co.mini.model.table.TtGameResult;
import jp.co.mini.model.table.TtTeam;
import jp.co.mini.repository.GameResultCrudRepository;
import jp.co.mini.repository.TeamCrudRepository;
import jp.co.mini.util.DateTimeUtil;

/**
 * レーティング計算サービス
 */
@Service
public class CalcRateService {

	@Autowired
	private GameResultCrudRepository gameResultCrudRepository;
	@Autowired
	private TeamCrudRepository teamCrudRepository;
	
	// TODO: 外部定義ファイルからK値を指定できるように変更したい。
	private static final Integer K_FACTOR = 32;
	private static final double RESULT_WIN = 1;
	private static final double RESULT_LOSE = 0;
	private static final double RESULT_DRAW = 0.5;
	
	private static final int ALL_YEAR = 9999;
	
	/**
	 * レーティング計算を実行する。（イロレーティング方式）
	 * 
	 * @param year 計算開始年度
	 * @param calcFlag 計算済フラグ
	 * 
	 * @return レーティング計算結果
	 */
	@Transactional
	public CalcRateResult calc(Integer year, boolean calcFlag) {
		CalcRateResult calcRateResult = new CalcRateResult();
		String nowDt = DateTimeUtil.getNowDateStr(ApplicationConstants.DATETIME_FORMAT_SYSTEM);
		
		List<TtGameResult> calcTargetResultList = gameResultCrudRepository.findByCalcFlag("0");
		// 計算対象年度以降の試合のみ抽出
		if (year != ALL_YEAR) {
			calcTargetResultList = calcTargetResultList.stream()
					.filter(target -> Integer.parseInt(target.getYear()) >= year).toList();
		}
		List<TtTeam> teamList = teamCrudRepository.findAll();
		Map<String, TtTeam> teamMap = teamList.stream().collect(Collectors.toMap(TtTeam::getTeamShortName, t -> t));
		
		// 未設定のチーム情報を確認
		List<String> teamNameList = new ArrayList<>();
		for (TtGameResult calcTargetResult : calcTargetResultList) {
			teamNameList.add(calcTargetResult.getHome());
			teamNameList.add(calcTargetResult.getAway());
		}
		teamNameList = teamNameList.stream().distinct().toList();
		
		List<String> unknownTeamNameList = teamNameList.stream().filter(name -> !teamMap.containsKey(name))
				.collect(Collectors.toList());
		
		if(!CollectionUtils.isEmpty(unknownTeamNameList)) {
			// レーティング計算対象に未設定のチーム情報が存在する場合、レーティング計算を行わない。
			calcRateResult.setUnknownTeamNameList(unknownTeamNameList);
			return calcRateResult;
		}
		
		for(TtGameResult calcTargetResult : calcTargetResultList) {
			// 共通設定
			if (calcFlag) {
				calcTargetResult.setCalcFlag("1");
			}
			calcTargetResult.setUpdateDate(nowDt);
			
			// 試合前レートを設定
			TtTeam homeTeam = teamMap.get(calcTargetResult.getHome());
			TtTeam awayTeam = teamMap.get(calcTargetResult.getAway());
			calcTargetResult.setHomeRate(homeTeam.getHomeRate());
			calcTargetResult.setAwayRate(awayTeam.getAwayRate());
			
			double homeResult = 0;
			double awayResult = 0;
			if (calcTargetResult.getResult().equals(GameResult.HOME_WIN.getCode())) {
				homeResult = RESULT_WIN;
				awayResult = RESULT_LOSE;
			} else if (calcTargetResult.getResult().equals(GameResult.AWAY_WIN.getCode())) {
				homeResult = RESULT_LOSE;
				awayResult = RESULT_WIN;
			} else {
				// 引き分けは双方0.5勝として計算
				homeResult = RESULT_DRAW;
				awayResult = RESULT_DRAW;
			}
			
			// 期待値の算出
			double expectedHomeWin = getExpectedScore(calcTargetResult.getHomeRate(), calcTargetResult.getAwayRate());
			double expectedAwayWin = getExpectedScore(calcTargetResult.getAwayRate(), calcTargetResult.getHomeRate());
			
			// レーティング計算
			int homeRate = updateRating(calcTargetResult.getHomeRate(), expectedHomeWin, homeResult);
			int awayRate = updateRating(calcTargetResult.getAwayRate(), expectedAwayWin, awayResult);
			homeTeam.setHomeRate(homeRate);
			awayTeam.setAwayRate(awayRate);
			
			homeTeam.setUpdateDate(nowDt);
			awayTeam.setUpdateDate(nowDt);
		}
		
		gameResultCrudRepository.saveAll(calcTargetResultList);
		teamCrudRepository.saveAll(teamList);
		
		calcRateResult.setGameResultList(calcTargetResultList);
		calcRateResult.setClubTeamInfoList(teamList);
		
		return calcRateResult;
	}

	// レーティングから期待勝率を算出する
	// 予測勝率 = 1 / (1 + 10^((playerRating - opponentRating) / 400))
	public double getWinRate(int playerRating, int opponentRating) {
		double ratingDifference = opponentRating - playerRating;
		double exponent = ratingDifference / 400.0;
		return 1.0 / (1.0 + Math.pow(10, exponent));
	}
	
	// 実力差に応じたスコアの期待値を計算する関数
	private double getExpectedScore(int rating1, int rating2) {
		double expectedScore = 1.0 / (1.0 + Math.pow(10.0, (rating2 - rating1) / 400.0));
		return expectedScore;
	}

	// レーティングを更新する関数
	private int updateRating(int rating, double score,  double result) {
		int newRating = (int) Math.round(rating + K_FACTOR * (result - score));
		return newRating;
	}
	
	/**
	 * レーティング計算結果を初期化する
	 */
	@Transactional
    public void reset() {
		List<TtGameResult> gameResultList = gameResultCrudRepository.findAll();
    	for (TtGameResult gameResult : gameResultList) {
    		gameResult.setHomeRate(null);
    		gameResult.setAwayRate(null);
    		gameResult.setCalcFlag("0");
    	}
    	gameResultCrudRepository.saveAll(gameResultList);
    	
		List<TtTeam> teamList = teamCrudRepository.findAll();
		for (TtTeam team : teamList) {
			team.setHomeRate(ApplicationConstants.DEFAULT_RATE);
			team.setAwayRate(ApplicationConstants.DEFAULT_RATE);
		}
		teamCrudRepository.saveAll(teamList);
		
	}
}
