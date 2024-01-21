package jp.co.mini.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.mini.code.GameResult;
import jp.co.mini.model.MatchingResult;
import jp.co.mini.model.MatchingResult.MatchingGameInfo;
import jp.co.mini.model.table.TmHistogram;
import jp.co.mini.model.table.TtTeam;
import jp.co.mini.repository.TeamCrudRepository;

/**
 * 結果予測サービスクラス
 */
@Service
public class ProphetService {

	@Autowired
	private ScrapingService scrapingService;
	@Autowired
	private TeamCrudRepository teamCrudRepository;
	@Autowired
	private HistogramManageService histogramManageService;

	private static final Map<String, String> RESULT_STYLE_MAP = Map.of(
			GameResult.HOME_WIN.getName(), "background-color: blue",
			GameResult.AWAY_WIN.getName(), "background-color: red",
			GameResult.DRAW.getName(), "background-color: yellow");

	private static final Map<String, String> TYPE_STYLE_MAP = Map.of(
			"typeA", "color: crimson",
			"typeB, ", "color: mediumblue");

	/**
	 * 次節の試合結果を予測する
	 * 
	 * @param type くじ種類
	 * @return 予測結果
	 */
	public List<MatchingResult> estimateMatchingResult(String type) {

		List<MatchingResult> matchingResultList = new ArrayList<>();

		if (type.equals("all")) {
			matchingResultList.addAll(execEstimateMatchingResult("typeA"));
			matchingResultList.addAll(execEstimateMatchingResult("typeB"));

		} else {
			matchingResultList.addAll(execEstimateMatchingResult(type));
		}

		// 確率が高い順にソート
		matchingResultList = modifyResultForDisp(matchingResultList);

		return matchingResultList;
	}

	/**
	 * 次節の試合結果を予測する
	 * 
	 * @param type くじ種類
	 * @return 予測結果
	 */
	private List<MatchingResult> execEstimateMatchingResult(String type) {

		List<MatchingResult> matchingResultList = new ArrayList<>();

		// 次節の組み合わせを収集する
		List<MatchingGameInfo> gameInfoList = new ArrayList<>();
		try {
			gameInfoList = scrapingService.scrapingMiniTotoMatches(type);
		} catch (Exception e) {
			throw new RuntimeException("MiniTotoの組み合わせ収集に失敗しました。");
		}

		// 次節のチーム一覧を生成する
		String[] teams = generateMatchingTeams(gameInfoList);

		// 次節の試合情報と予測パターンを総当たりで生成する
		List<MatchingGameInfo> matchingGameInfoList = generateMatches(teams);

		// 各試合の勝率を個別算出
		List<TmHistogram> histogramList = histogramManageService.getHistogram();
		Map<String, TtTeam> teamInfoMap = getTeamInfo(Arrays.asList(teams));
		for (MatchingGameInfo matchingGameInfo : matchingGameInfoList) {
			TtTeam homeTeam = teamInfoMap.get(matchingGameInfo.getHome());
			TtTeam awayTeam = teamInfoMap.get(matchingGameInfo.getAway());
			estimateMatchingByHistogram(histogramList, homeTeam.getHomeRate(), awayTeam.getAwayRate(),
					matchingGameInfo);
		}

		// 予測ごとの発生確率を算出
		matchingResultList = estimateProbability(matchingGameInfoList);

		// くじ種別を設定
		for (MatchingResult matchingResult : matchingResultList) {
			matchingResult.setType(type);
			matchingResult.setTypeStyle(TYPE_STYLE_MAP.get(type));
		}
		matchingResultList.stream().forEach(result -> result.setType(type));

		return matchingResultList;
	}

	/**
	 * 次節に試合を行うチームの一覧を生成する<br>
	 * MINI TOTOは5試合固定なので、チーム数を10チーム固定とする。<br>
	 * 
	 * @param gameInfoList 次節の試合一覧
	 * @return 次節のチーム一覧
	 */
	private String[] generateMatchingTeams(List<MatchingGameInfo> gameInfoList) {
		String[] matchingTeams = new String[10];
		int teamIndex = 0;
		for (MatchingGameInfo gameInfo : gameInfoList) {
			matchingTeams[teamIndex] = gameInfo.getHome();
			teamIndex++;
			matchingTeams[teamIndex] = gameInfo.getAway();
			teamIndex++;
		}

		return matchingTeams;
	}

	/**
	 * 試合ごとの予想パターンを総当たりで作成する
	 * 
	 * @param teams チーム
	 * @return  試合ごとの予想パターン全件
	 */
	public List<MatchingGameInfo> generateMatches(String[] teams) {
		List<MatchingGameInfo> matchingGameInfoList = new ArrayList<>();

		// 試合結果３パターン（ホーム勝ち、アウェイ勝つ、ドロー）
		String[] results = new String[3];
		results[0] = GameResult.HOME_WIN.getName();
		results[1] = GameResult.AWAY_WIN.getName();
		results[2] = GameResult.DRAW.getName();

		int matchCount = teams.length / 2;
		int resultCount = results.length;
		int[] indices = new int[matchCount];

		// 試合ごとの予測の組み合わせを総当たりで作成
		while (true) {

			for (int i = 0; i < matchCount; i++) {
				int teamIndex = i * 2;
				MatchingGameInfo matchingGameInfo = new MatchingGameInfo();
				matchingGameInfo.setHome(teams[teamIndex]);
				matchingGameInfo.setAway(teams[teamIndex + 1]);
				matchingGameInfo.setProphetInfo(results[indices[i]]);
				matchingGameInfo.setProphetInfoStyle(RESULT_STYLE_MAP.get(matchingGameInfo.getProphetInfo()));
				matchingGameInfoList.add(matchingGameInfo);
			}

			int j = matchCount - 1;
			while (j >= 0 && indices[j] == resultCount - 1) {
				indices[j] = 0;
				j--;
			}

			if (j < 0) {
				break;
			}

			indices[j]++;
		}

		return matchingGameInfoList;
	}

	/**
	 * ヒストグラムを利用して試合結果を予測する
	 * 
	 * @param histogramList ヒストグラム情報
	 * @param homeRate ホームレート
	 * @param awayRate アウェイレート
	 * @param matchingGameInfo 試合結果予測情報
	 */
	private void estimateMatchingByHistogram(List<TmHistogram> histogramList, int homeRate, int awayRate,
			MatchingGameInfo matchingGameInfo) {
		// 対戦チームのレート差
		int rateWidth = Math.abs(homeRate - awayRate);
		// True: ホーム側のレートのほうが高い場合、False: アウェイ側のレートのほうが高い場合
		boolean isHomeRateHigher = homeRate >= awayRate;

		for (TmHistogram histogram : histogramList) {

			if (rateWidth < histogram.getRateWidth()) {
				// ヒストグラム情報から、次節のホーム勝率、アウェイ勝率、引き分け率を算出する
				double winPercent = histogram.getWinPercent();
				double losePercent = histogram.getLosePercent();
				double drawPercent = histogram.getDrawPercent();

				// 個別の一致確率を設定
				double prophetPercent = 0;

				if (matchingGameInfo.getProphetInfo().equals(GameResult.HOME_WIN.getName())) {

					if (isHomeRateHigher) {
						prophetPercent = winPercent;
					} else {
						prophetPercent = losePercent;
					}

				} else if (matchingGameInfo.getProphetInfo().equals(GameResult.AWAY_WIN.getName())) {

					if (isHomeRateHigher) {
						prophetPercent = losePercent;
					} else {
						prophetPercent = winPercent;
					}

				} else if (matchingGameInfo.getProphetInfo().equals(GameResult.DRAW.getName())) {
					prophetPercent = drawPercent;
				}

				double scale = 1000; // 小数点以下第三位までを残すための倍数
				prophetPercent = Math.floor(prophetPercent / 100 * scale) / scale;
				matchingGameInfo.setProphetPercent(prophetPercent);

				// 画面表示用にフォーマットする
				if (isHomeRateHigher) {
					matchingGameInfo.setWinPercent(String.format("%." + 2 + "f%%", winPercent));
					matchingGameInfo.setLosePercent(String.format("%." + 2 + "f%%", losePercent));
				} else {
					matchingGameInfo.setWinPercent(String.format("%." + 2 + "f%%", losePercent));
					matchingGameInfo.setLosePercent(String.format("%." + 2 + "f%%", winPercent));
				}
				matchingGameInfo.setDrawPercent(String.format("%." + 2 + "f%%", drawPercent));

				matchingGameInfo.setHomeRate(homeRate);
				matchingGameInfo.setAwayRate(awayRate);

				break;

			}

		}

	}

	/**
	 * 予測ごとの発生確率を算出
	 * 
	 * @param matchingGameInfoList 試合情報リスト
	 * @return 予測結果リスト
	 */
	private List<MatchingResult> estimateProbability(List<MatchingGameInfo> matchingGameInfoList) {
		List<MatchingResult> matchingResultList = new ArrayList<>();

		// 試合情報のカーソル（5件1セットの識別に利用）
		int matchingGameRow = 1;
		// 発生確率
		double probability = 0;
		MatchingResult matchingResult = new MatchingResult();
		List<MatchingGameInfo> matchingGameInfoItems = new ArrayList<>();

		for (MatchingGameInfo matchingGameInfo : matchingGameInfoList) {
			matchingGameInfoItems.add(matchingGameInfo);

			// 発生確率を5件1セットで算出
			if (probability == 0) {
				probability = matchingGameInfo.getProphetPercent();
			} else {
				probability = probability * matchingGameInfo.getProphetPercent();
			}

			if (matchingGameRow % 5 == 0) {
				// 5件1セットで登録する
				List<MatchingGameInfo> matchingGameInfoCopys = new ArrayList<>();
				for (MatchingGameInfo matchingGameInfoItem : matchingGameInfoItems) {
					// 参照渡しで上書きされないようにBeanをコピーする
					MatchingGameInfo matchingGameInfoCopy = new MatchingGameInfo();
					BeanUtils.copyProperties(matchingGameInfoItem, matchingGameInfoCopy);
					matchingGameInfoCopys.add(matchingGameInfoCopy);
				}
				matchingResult.setMatchingGameInfo(matchingGameInfoCopys);
				probability = probability * 100;
				if (probability >= 1) {
					matchingResult.setProbabilityStyle("background-color: palegreen;");
				}
				matchingResult.setProbability(String.format("%." + 2 + "f%%", probability));
				matchingResultList.add(matchingResult);

				// 初期化
				matchingResult = new MatchingResult();
				matchingGameInfoItems = new ArrayList<>();
				probability = 0;
			}

			matchingGameRow++;
		}

		return matchingResultList;
	}

	/**
	 * 画面表示用に結果を整形する。
	 * 
	 * @param matchingResultList 予測結果リスト
	 * @return 予測結果リスト
	 */
	private List<MatchingResult> modifyResultForDisp(List<MatchingResult> matchingResultList) {
		// 確率が高い順にソート
		List<MatchingResult> matchingResultListSorted = matchingResultList.stream()
				.sorted(Comparator
						.comparingDouble(result -> extractProbability(((MatchingResult) result).getProbability()))
						.reversed())
				.toList();

		// 予想Noを設定
		int prophetNo = 1;
		for (MatchingResult matchingResultSorted : matchingResultListSorted) {
			matchingResultSorted.setProphetNo(prophetNo);
			prophetNo++;
		}

		return matchingResultListSorted;
	}

	/**
	 * チーム情報を取得する。
	 * 
	 * @param teamShortNameList チーム略称
	 * @return チーム情報MAP {チーム略称 : チーム情報}
	 */
	private Map<String, TtTeam> getTeamInfo(List<String> teamShortNameList) {
		List<TtTeam> teamList = teamCrudRepository.findByTeamShortNameIn(teamShortNameList);
		Map<String, TtTeam> teamMap = teamList.stream()
				.collect(Collectors.toMap(TtTeam::getTeamShortName, team -> team));

		return teamMap;
	}

	/**
	 * 発生確率文字列の%を取り除き、数値に変換する。
	 * 
	 * @param probablity 発生確率（文字列）
	 * @return 発生確率（数値）
	 */
	private double extractProbability(String probablity) {
		String value = probablity.replace("%", "");
		return Double.parseDouble(value);
	}

}
