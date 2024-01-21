package jp.co.mini.model;

import java.util.ArrayList;
import java.util.List;

import jp.co.mini.model.table.TmHistogram;
import lombok.Data;

/**
 * 予測結果情報
 */
@Data
public class HistogramData {

	private int recordNumber0;
	private double winPercent0;
	private double losePercent0;
	private double drawPercent0;
	private int rateWidth0;
	private String description0;
	
	private int recordNumber1;
	private double winPercent1;
	private double losePercent1;
	private double drawPercent1;
	private int rateWidth1;
	private String description1;

	private int recordNumber2;
	private double winPercent2;
	private double losePercent2;
	private double drawPercent2;
	private int rateWidth2;
	private String description2;

	private int recordNumber3;
	private double winPercent3;
	private double losePercent3;
	private double drawPercent3;
	private int rateWidth3;
	private String description3;

	private int recordNumber4;
	private double winPercent4;
	private double losePercent4;
	private double drawPercent4;
	private int rateWidth4;
	private String description4;

	private int recordNumber5;
	private double winPercent5;
	private double losePercent5;
	private double drawPercent5;
	private int rateWidth5;
	private String description5;

	private int recordNumber6;
	private double winPercent6;
	private double losePercent6;
	private double drawPercent6;
	private int rateWidth6;
	private String description6;

	private int recordNumber7;
	private double winPercent7;
	private double losePercent7;
	private double drawPercent7;
	private int rateWidth7;
	private String description7;

	private int recordNumber8;
	private double winPercent8;
	private double losePercent8;
	private double drawPercent8;
	private int rateWidth8;
	private String description8;

	private int recordNumber9;
	private double winPercent9;
	private double losePercent9;
	private double drawPercent9;
	private int rateWidth9;
	private String description9;

	private int recordNumber10;
	private double winPercent10;
	private double losePercent10;
	private double drawPercent10;
	private int rateWidth10;
	private String description10;

	public List<TmHistogram> convertHistgram() {
		List<TmHistogram> histogramList = new ArrayList<>();

		histogramList.add(new TmHistogram(recordNumber0,
				winPercent0, losePercent0, drawPercent0, rateWidth0, description0));
		histogramList.add(new TmHistogram(recordNumber1,
				winPercent1, losePercent1, drawPercent1, rateWidth1, description1));
		histogramList.add(new TmHistogram(recordNumber2,
				winPercent2, losePercent2, drawPercent2, rateWidth2, description2));
		histogramList.add(new TmHistogram(recordNumber3,
				winPercent3, losePercent3, drawPercent3, rateWidth3, description3));
		histogramList.add(new TmHistogram(recordNumber4,
				winPercent4, losePercent4, drawPercent4, rateWidth4, description4));
		histogramList.add(new TmHistogram(recordNumber5,
				winPercent5, losePercent5, drawPercent5, rateWidth5, description5));
		histogramList.add(new TmHistogram(recordNumber6,
				winPercent6, losePercent6, drawPercent6, rateWidth6, description6));
		histogramList.add(new TmHistogram(recordNumber7,
				winPercent7, losePercent7, drawPercent7, rateWidth7, description7));
		histogramList.add(new TmHistogram(recordNumber8,
				winPercent8, losePercent8, drawPercent8, rateWidth8, description8));
		histogramList.add(new TmHistogram(recordNumber9,
				winPercent9, losePercent9, drawPercent9, rateWidth9, description9));
		histogramList.add(new TmHistogram(recordNumber10,
				winPercent10, losePercent10, drawPercent10, rateWidth10, description10));
		
		return histogramList;
	}

}
