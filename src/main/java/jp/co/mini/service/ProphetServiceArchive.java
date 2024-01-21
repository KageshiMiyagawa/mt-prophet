package jp.co.mini.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import jp.co.mini.model.GameResultData;
import jp.co.mini.model.ProphetResult;
import jp.co.mini.model.table.TtHistogram;
import jp.co.mini.model.table.TtTeam;
import jp.co.mini.repository.TeamCrudRepository;

/**
 * 結果予測サービスクラス(DEMO版 非推奨)
 */
@Deprecated
public class ProphetServiceArchive {

	@Autowired
	private ScrapingService scrapingService;
	@Autowired
	private TeamCrudRepository teamCrudRepository;
	@Autowired
	private HistogramManageService histogramManageService;

	/**
	 * 試合結果を予測する。<br>
	 * Jリーグ(J1～J3)の次節の試合情報を収集し、結果を予測する。<br>
	 * 結果は過去の試合結果から算出したヒストグラムと、各チームのレートから算出する。
	 * 
	 * @param league リーグ種別
	 * @return 予測結果
	 */
	@Deprecated 
	public List<ProphetResult> execute(String league) {

		// 次節の試合予定をスクレイピング
		List<GameResultData> gameResultList = scrapingService.scrapingGameResultForProphet(league);
		if (CollectionUtils.isEmpty(gameResultList)) {
			// 試合予定なし
			return new ArrayList<>();
		}
		// チーム情報MAP {チーム略称 : チーム情報}
		Map<String, TtTeam> teamMap = getTeamMap(gameResultList);
		// ヒストグラム情報
		List<TtHistogram> histogramList = histogramManageService.createHistogram();

		// 試合結果を予測
		return executeProphet(gameResultList, teamMap, histogramList);
	}

	/**
	 * チーム情報を取得する。
	 * 
	 * @param gameResultList 試合情報（次節）
	 * @return チーム情報MAP {チーム略称 : チーム情報}
	 */
	private Map<String, TtTeam> getTeamMap(List<GameResultData> gameResultList) {
		List<String> teamShortNameList = new ArrayList<>();
		teamShortNameList.addAll(gameResultList.stream().map(result -> result.getHome()).toList());
		teamShortNameList.addAll(gameResultList.stream().map(result -> result.getAway()).toList());
		teamShortNameList = teamShortNameList.stream().distinct().toList();

		List<TtTeam> teamList = teamCrudRepository.findByTeamShortNameIn(teamShortNameList);
		Map<String, TtTeam> teamMap = teamList.stream()
				.collect(Collectors.toMap(TtTeam::getTeamShortName, team -> team));

		return teamMap;
	}

	/**
	 * 試合結果を予測する
	 * 
	 * @param gameResultList 試合情報（次節）
	 * @param teamMap チーム情報MAP {チーム略称 : チーム情報}
	 * @param histogramList ヒストグラム情報
	 * @return 試合結果予測情報
	 */
	private List<ProphetResult> executeProphet(List<GameResultData> gameResultList, Map<String, TtTeam> teamMap,
			List<TtHistogram> histogramList) {
		List<ProphetResult> prophetResultList = new ArrayList<>();

		for (GameResultData gameResult : gameResultList) {
			ProphetResult prophetResult = new ProphetResult();

			// ヒストグラムを利用した予測以外の基礎情報を設定
			BeanUtils.copyProperties(gameResult, prophetResult);
			StringBuilder gameDateBuilder = new StringBuilder();
			gameDateBuilder.append(gameResult.getYear());
			gameDateBuilder.append("/");
			gameDateBuilder.append(gameResult.getGameDate());
			gameDateBuilder.append(" ");
			gameDateBuilder.append(gameResult.getGameTime());
			prophetResult.setGameDate(gameDateBuilder.toString());

			TtTeam homeTeam = teamMap.get(gameResult.getHome());
			prophetResult.setHomeRate(homeTeam.getHomeRate());
			TtTeam awayTeam = teamMap.get(gameResult.getAway());
			prophetResult.setAwayRate(awayTeam.getHomeRate());

			// ヒストグラムを利用した予測情報を設定
			setProphetByHistogram(histogramList, prophetResult);

			prophetResultList.add(prophetResult);
		}

		return prophetResultList;
	}

	/**
	 * ヒストグラムを利用して試合結果を予測する
	 * 
	 * @param histogramList ヒストグラム情報
	 * @param prophetResult 試合結果予測情報
	 */
	private void setProphetByHistogram(List<TtHistogram> histogramList, ProphetResult prophetResult) {
		// 対戦チームのレート差
		int rateWidth = Math.abs(prophetResult.getHomeRate() - prophetResult.getAwayRate());
		// True: ホーム側のレートのほうが高い場合、False: アウェイ側のレートのほうが高い場合
		boolean isHomeRateHigher = prophetResult.getHomeRate() >= prophetResult.getAwayRate();

		for (TtHistogram histogram : histogramList) {

			if (rateWidth < histogram.getRateWidth()) {
				// ヒストグラム情報から、次節のホーム勝率、アウェイ勝率、引き分け率を算出する
				int totalCount = histogram.getWinCount() + histogram.getLoseCount() + histogram.getDrawCount();
				double winP = (double) histogram.getWinCount() / totalCount * 100;
				double loseP = (double) histogram.getLoseCount() / totalCount * 100;
				double drawP = (double) histogram.getDrawCount() / totalCount * 100;
				// 画面表示用にフォーマットする
				if (isHomeRateHigher) {
					prophetResult.setWinP(String.format("%." + 2 + "f%%", winP));
					prophetResult.setLoseP(String.format("%." + 2 + "f%%", loseP));
				} else {
					prophetResult.setWinP(String.format("%." + 2 + "f%%", loseP));
					prophetResult.setLoseP(String.format("%." + 2 + "f%%", winP));
				}
				prophetResult.setDrawP(String.format("%." + 2 + "f%%", drawP));
				break;

			}

		}
	}

}
