package jp.co.mini.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.mini.code.GameResult;
import jp.co.mini.model.table.TmHistogram;
import jp.co.mini.model.table.TtGameResult;
import jp.co.mini.model.table.TtHistogram;
import jp.co.mini.repository.GameResultCrudRepository;
import jp.co.mini.repository.HistogramRepository;
import jp.co.mini.repository.HistogramRepositoryArchive;

/**
 * ヒストグラム管理サービス
 */
@Service
public class HistogramManageService {

	@Autowired
	private GameResultCrudRepository gameResultCrudRepository;
	@Autowired
	private HistogramRepositoryArchive histogramRepositoryArchive;
	@Autowired
	private HistogramRepository histogramRepository;

	/**
	 * ヒストグラム情報を作成する。<br>
	 * 過去の試合結果をもとに、レート差ごとの勝ち数、負け数、引き分け数をカウントする。
	 * 
	 * @return ヒストグラム情報
	 */
	@Transactional
	public List<TtHistogram> createHistogram() {
		// ヒストグラム未割当の試合結果を抽出
		List<TtGameResult> ttGameResultList = gameResultCrudRepository.findAll();
		// TODO: StreamAPIでフィルタしているが、SQL抽出時に条件指定するように修正する。
		ttGameResultList = ttGameResultList.stream().filter(game -> game.getHistogramFlag().equals("0")).toList();

		// ヒストグラム情報を抽出
		List<TtHistogram> histogramList = histogramRepositoryArchive.findAll();
		histogramList = histogramList.stream().sorted(Comparator.comparingInt(TtHistogram::getRateWidth))
				.collect(Collectors.toList());

		for (TtGameResult ttGameResult : ttGameResultList) {
			int rateWidth = Math.abs(ttGameResult.getHomeRate() - ttGameResult.getAwayRate());
			String result = ttGameResult.getResult();
			boolean isHomeRateHigher = ttGameResult.getHomeRate() >= ttGameResult.getAwayRate();

			for (TtHistogram histogram : histogramList) {

				if (rateWidth < histogram.getRateWidth()) {

					if (GameResult.DRAW.getCode().equals(result)) {
						histogram.setDrawCount(histogram.getDrawCount() + 1);
					} else if (isHomeRateHigher && GameResult.HOME_WIN.getCode().equals(result)
							|| !isHomeRateHigher && GameResult.AWAY_WIN.getCode().equals(result)) {
						histogram.setWinCount(histogram.getWinCount() + 1);
					} else {
						histogram.setLoseCount(histogram.getLoseCount() + 1);
					}
					
					System.out.println(histogram + "home:" + ttGameResult.getHomeRate() + ",away:" + ttGameResult.getAwayRate());

					break;
				}

			}

			ttGameResult.setHistogramFlag("1");
		}

		histogramRepositoryArchive.saveAll(histogramList);
		gameResultCrudRepository.saveAll(ttGameResultList);
		
		return histogramList;
	}
	
	@Transactional
	public List<TmHistogram> getHistogram() {
		return histogramRepository.findAll();
	}
	
	@Transactional
	public List<TmHistogram> saveHistogram(List<TmHistogram> histogramList) {
		return histogramRepository.saveAll(histogramList);
	}

}
