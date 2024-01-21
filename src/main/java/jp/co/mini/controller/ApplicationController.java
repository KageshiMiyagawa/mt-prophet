package jp.co.mini.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jp.co.mini.code.ScrapingType;
import jp.co.mini.model.CalcRateResult;
import jp.co.mini.model.ClubTeamInfo;
import jp.co.mini.model.GameResultData;
import jp.co.mini.model.HistogramData;
import jp.co.mini.model.MatchingResult;
import jp.co.mini.model.table.TmHistogram;
import jp.co.mini.service.CalcRateService;
import jp.co.mini.service.HistogramManageService;
import jp.co.mini.service.ProphetService;
import jp.co.mini.service.ScrapingService;
import jp.co.mini.service.TeamManageService;
import jp.co.mini.util.DateTimeUtil;

/**
 * コントローラークラス
 */
@Controller
public class ApplicationController {

	@Autowired
	private ScrapingService scrapingService;
	@Autowired
	private CalcRateService calcRateService;
	@Autowired
	private ProphetService prophetService;
	@Autowired
	private TeamManageService teamManageService;
	@Autowired
	private HistogramManageService histogramManageService;
    
	/**
	 * アプリケーションデフォルト画面の表示
	 * 
	 * @param model Thymeleafモデルクラス
	 * @return テンプレートマッピング用の文字列
	 */
    @GetMapping({"/", "/index"})
    public String getDefaultAccess(Model model) {
    	return "index";
    }
	
    /**
     * スクレイピング画面の表示
     * 
	 * @param model Thymeleafモデルクラス
	 * @return テンプレートマッピング用の文字列
	 */
    @GetMapping("/scraping")
    public String dispScraping(Model model){
    	generateTargetYears(model);
    	return "scraping";
    }
    
    /**
     * スクレイピング処理を実行
     * 
     * @param model Thymeleafモデルクラス
     * @param type スクレイピング種別
     * @param year スクレイピング対象年
     * @return テンプレートマッピング用の文字列
     */
    @PostMapping("/scraping/{type}")
    public String scraping(Model model, @PathVariable String type, @RequestParam(required = false) Integer year){
    	if (ScrapingType.GAME_RESULT.getType().equals(type)) {
    		List<GameResultData> gameResultList = scrapingService.scrapingGameResult(year);
    		model.addAttribute("gameResultList", gameResultList);
    		model.addAttribute("selectedFormType", "gameResult");
    		
    	} else if (ScrapingType.TEAM_INFO.getType().equals(type)) {
    		List<ClubTeamInfo> clubTeamInfoList = scrapingService.scrapingClubTeamInfo();
    		model.addAttribute("clubTeamInfoList", clubTeamInfoList); 
    		model.addAttribute("selectedFormType", "teamInfo");
    	}
    	
    	generateTargetYears(model);
    	return "scraping";
    }
    
    /**
     * レーティング計算画面を表示
     * 
     * @param model Thymeleafモデルクラス
     * @return テンプレートマッピング用の文字列
     */
    @GetMapping("/calc")
    public String dispCalc(Model model){
    	return "calc";
    }
    
    /**
     * レーティング計算を実行
     * 
     * @param model Thymeleafモデルクラス
     * @return テンプレートマッピング用の文字列
     */
    @PostMapping("/calc")
    public String postCalc(Model model, 
    		@RequestParam(required = false, defaultValue = "9999") Integer year,
    		@RequestParam(required = false, defaultValue = "1") Integer count,
    		@RequestParam(required = true) String option){
    	
    	CalcRateResult result = execCalc(year, count);
    	
    	if (!CollectionUtils.isEmpty(result.getUnknownTeamNameList())) {
    		
        	if (option.equals("force")) {
        		// 未登録チーム強制登録の場合、未登録チームを初期レートで登録する。
        		teamManageService.saveUnknownTeam(result.getUnknownTeamNameList());
        		result = execCalc(year, count);
        	} else {
        		// 通常計算の場合、未登録チームを画面表示して計算は行わない。
        		model.addAttribute("unknownTeamNameList", result.getUnknownTeamNameList());
        		return "calc";
        	}
    		
    	}
    	
    	model.addAttribute("gameResultList", result.getGameResultList());
    	model.addAttribute("clubTeamInfoList", result.getClubTeamInfoList()); 
    	return "calc";
    }
    
    /**
     * レーティング計算を実行する
     * @param year 計算開始年度
     * @param count 繰り返し回数
     * @return 計算結果
     */
    private CalcRateResult execCalc(int year, int count) {
    	CalcRateResult result = null;
    	for (int i = 1; i <= count; i++) {
    		boolean calcFlag = false;
    		if (i == count) {
    			calcFlag = true;
    		}
    		result = calcRateService.calc(year, calcFlag);
    	}
    	return result;
    }

    /**
     * レーティング計算を実行
     * 
     * @param model Thymeleafモデルクラス
     * @return テンプレートマッピング用の文字列
     */
    @PostMapping("/reset-calc")
    public String postResetCalc(Model model){
    	
    	calcRateService.reset();
    	model.addAttribute("infoMessage", "レーティング計算結果を初期化しました。");

    	return "calc";
    }
    
    /**
     * 試合結果予測画面を表示
     * 
     * @param model Thymeleafモデルクラス
     * @return テンプレートマッピング用の文字列
     */
    @GetMapping("/prophet")
    public String dispProphet(Model model){
    	return "prophet";
    }

    @PostMapping("/prophet")
	public String postProphetList(Model model,
			@RequestParam(required = true) String type) {
    	List<MatchingResult> matchingResultList = prophetService.estimateMatchingResult(type);
    	model.addAttribute("matchingResultList", matchingResultList); 
    	return "prophet";
    }
    
    /**
     * 試合結果予測画面を表示
     * 
     * @param model Thymeleafモデルクラス
     * @return テンプレートマッピング用の文字列
     */
    @GetMapping("/histogram")
    public String dispHistogram(Model model){
    	List<TmHistogram> histogramInfoList = histogramManageService.getHistogram();
    	model.addAttribute("histogramInfoList", histogramInfoList);
    	return "histogram";
    }

    @PostMapping("/histogram")
	public String postHistogram(Model model, @ModelAttribute HistogramData histogramData) {
    	List<TmHistogram> histogramInfoList= histogramManageService.saveHistogram(histogramData.convertHistgram());
    	model.addAttribute("histogramInfoList", histogramInfoList);
    	return "histogram";
    }
    
    /**
     * 対象年を画面パラメータに設定する（現在年から過去5年を設定）
     * @param model Thymeleafモデルクラス
     */
    private void generateTargetYears(Model model) {
    	List<Integer> targetYears = DateTimeUtil.getPastYears(5);
    	model.addAttribute("targetYears", targetYears);
    }
         
}