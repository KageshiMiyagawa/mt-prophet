package jp.co.mini.service;

import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.micrometer.common.util.StringUtils;
import jp.co.mini.ApplicationConstants;
import jp.co.mini.code.GameResult;
import jp.co.mini.code.League;
import jp.co.mini.model.ClubTeamInfo;
import jp.co.mini.model.GameResultData;
import jp.co.mini.model.MatchingResult.MatchingGameInfo;
import jp.co.mini.model.table.TtCollectManage;
import jp.co.mini.repository.CollectManageCrudRepository;
import jp.co.mini.repository.GameResultCustomRepository;
import jp.co.mini.repository.TeamCustomRepository;
import jp.co.mini.util.DateTimeUtil;
import jp.co.mini.util.StringUtil;

/**
 * スクレイピングサービスクラス
 */
@Service
public class ScrapingService {

	@Autowired
	private GameResultCustomRepository gameResultCustomRepository;
	@Autowired
	private CollectManageCrudRepository collectManageCrudRepository;
	@Autowired
	private TeamCustomRepository teamCustomRepository;

	private final Logger logger = LoggerFactory.getLogger("");

	private static final String J_LEAGUE_DATA_SITE_URL = "https://data.j-league.or.jp/SFMS01/search";
	private static final String J_LEAGUE_CLUB_TEAM_URL_J1 = "https://data.j-league.or.jp/SFTP01/?startPage=0&endPage=5&competitionFrameId=1&prev_next=&nextBtnVal=0&prevBtnVal=0";
	private static final String J_LEAGUE_CLUB_TEAM_URL_J2 = "https://data.j-league.or.jp/SFTP01/?startPage=0&endPage=5&competitionFrameId=2&prev_next=&nextBtnVal=0&prevBtnVal=0";
	private static final String J_LEAGUE_CLUB_TEAM_URL_J3 = "https://data.j-league.or.jp/SFTP01/?startPage=0&endPage=5&competitionFrameId=3&prev_next=&nextBtnVal=0&prevBtnVal=0";

	private static final String TOTO_FORM = "https://store.toto-dream.com/dcs/subos/screen/ps01/spsl000/PGSPSL00001MoveTotoLoto.form";

	private static final String SCRAPING_TYPE_COLLECT = "1";
	private static final String SCRAPING_TYPE_PROPHET = "2";
	private static final List<String> PROPHET_TARGET_LEAGUE = Arrays
			.asList(new String[] { League.J1.getName(), League.J2.getName(), League.J3.getName() });

	private static final DateTimeFormatter DTF_SYS = DateTimeFormatter
			.ofPattern(ApplicationConstants.DATETIME_FORMAT_SYSTEM);

	private static final int MAX_RETRY_COUNT = 3;
	
	private static final int FULL_YEAR_PREFIX = 9999;
	private static final int OLDEST_SEARCH_YEAR = 1992;

	/**
	 * 試合結果を収集する。年度の指定がない場合は最新年度について収集する。
	 * 
	 * @param year 収集開始年度
	 * @return 収集結果
	 */
	@Transactional
	public List<GameResultData> scrapingGameResult(Integer year) {
		List<GameResultData> resultList = new ArrayList<>();
		String nowDt = DateTimeUtil.getNowDateStr(ApplicationConstants.DATETIME_FORMAT_SYSTEM);
		TtCollectManage lastCollectManage = null;

		// スクレイピングの最終収集日時を抽出。初回収集でレコードがない場合は新規作成する。
		Optional<TtCollectManage> collectManage = collectManageCrudRepository
				.findById(ApplicationConstants.LAST_COLLECT_MANAGE_ID_GAME_RESULT);
		if (collectManage.isEmpty()) {
			lastCollectManage = new TtCollectManage();
			lastCollectManage.setCollectId(ApplicationConstants.LAST_COLLECT_MANAGE_ID_GAME_RESULT);
			lastCollectManage.setCollectName(ApplicationConstants.LAST_COLLECT_MANAGE_NAME_GAME_RESULT);
		} else {
			lastCollectManage = collectManage.get();
		}

		Integer currentYear = DateTimeUtil.getCurrentYear();
		Integer targetYear = year;
		if (Objects.isNull(targetYear)) {
			targetYear = currentYear;
		}
		if (targetYear == FULL_YEAR_PREFIX) {
			targetYear = OLDEST_SEARCH_YEAR;
		}

		// 現在年の情報までスクレイピングを実行する
		while (currentYear.compareTo(targetYear) >= 0) {
			resultList.addAll(
					execScraping(targetYear, lastCollectManage.getLastCollectDate(), SCRAPING_TYPE_COLLECT));
			targetYear++;
		}

		// スクレイピング結果を保存する
		gameResultCustomRepository.saveGameResult(resultList, nowDt);
		lastCollectManage.setLastCollectDate(nowDt);
		collectManageCrudRepository.save(lastCollectManage);

		return resultList;
	}

	/**
	 * 試合情報を収集する。（結果予測用）<br>
	 * 試合前の情報のみを収集する。<br>
	 * リーグが指定された場合は対象リーグのみを収集する。
	 * 
	 * @param league リーグ(J1、J2、J3）
	 * @return 収集結果
	 */
	public List<GameResultData> scrapingGameResultForProphet(String league) {
		Optional<TtCollectManage> collectManage = collectManageCrudRepository
				.findById(ApplicationConstants.LAST_COLLECT_MANAGE_ID_GAME_RESULT);
		if (collectManage.isEmpty()) {
			throw new RuntimeException("スクレイピング未実施のため予測できません");
		}

		// 次節の試合情報をスクレイピング
		List<GameResultData> resultList = execScraping(DateTimeUtil.getCurrentYear(),
				collectManage.get().getLastCollectDate(),
				SCRAPING_TYPE_PROPHET);

		// J1、J2、J3以外の大会情報を除外
		if (StringUtils.isNotEmpty(league)) {
			List<String> expect = Arrays
					.asList(new String[] { StringUtil.toFullWidth(league), StringUtil.toHalfWidth(league) });
			resultList = resultList.stream().filter(result -> expect.contains(result.getTournament())).toList();
		}

		// J1、J2、J3ごとにグルーピング
		Map<String, List<GameResultData>> resultMap = resultList.stream()
				.collect(Collectors.groupingBy(GameResultData::getTournament));

		// 対象のリーグの情報以外を除外
		List<GameResultData> fileterResultList = new ArrayList<>();
		for (Map.Entry<String, List<GameResultData>> entry : resultMap.entrySet()) {
			String targetSection = entry.getValue().stream().findFirst().get().getSection();
			fileterResultList.addAll(
					entry.getValue().stream().filter(result -> targetSection.equals(result.getSection())).toList());
		}

		return fileterResultList;
	}

	/**
	 * スクレイピング処理を実行する。
	 * @param targetYear 対象年
	 * @param lastCollectDate 最終収集日時
	 * @param scrapingType 収集種別
	 * @return 収集結果
	 */
	private List<GameResultData> execScraping(Integer targetYear, String lastCollectDate, String scrapingType) {
		List<GameResultData> resultList = new ArrayList<>();
		boolean checkDateFlg = false;
		boolean collectedFlg = false;

		if (StringUtils.isNotBlank(lastCollectDate)) {
			LocalDate date = LocalDate.parse(lastCollectDate, DTF_SYS);
			int lastCollectedYear = date.getYear();
			if (lastCollectedYear > targetYear) {
				// 収集済みのためSkip
				return resultList;
			}
			if (lastCollectedYear == targetYear) {
				// 最終収集年を収集中の場合は、最終収集日時と比較するフラグをONにする
				checkDateFlg = true;
			}
		}

		try {
			String condition = "competition_years=" + targetYear;
			String url = J_LEAGUE_DATA_SITE_URL + "?" + condition;

			Document doc = getJsoupResult(url);

			Element table = doc.select("table").first();
			Elements trElements = table.select("tr");

			for (int i = 0; i < trElements.size(); i++) {
				if (i == 0) {
					continue;
				}
				if (collectedFlg) {
					break;
				}

				Element trElement = trElements.get(i);
				Elements tdElements = trElement.select("td");
				GameResultData result = new GameResultData();

				for (int j = 0; j < tdElements.size(); j++) {
					Element tdElement = tdElements.get(j);
					String data = tdElement.text();
					if (j == 0)
						result.setYear(data);
					else if (j == 1)
						result.setTournament(data);
					else if (j == 2)
						result.setSection(data);
					else if (j == 3)
						result.setGameDate(data);
					else if (j == 4)
						result.setGameTime(data);
					else if (j == 5)
						result.setHome(data);
					else if (j == 6)
						result.setScore(data);
					else if (j == 7)
						result.setAway(data);
					else if (j == 8)
						result.setStadium(data);
				}

				if (scrapingType.equals(SCRAPING_TYPE_COLLECT)) {

					if (checkDateFlg) {

						if (result.getScore().equals("vs")) {
							// 試合前のため収集対象外、以降はすべて対象外のため処理不要
							collectedFlg = true;
							break;
						}

						String gameDateFmt = result.getGameDate().substring(0, 5);
						String lastCollectDateFmt = DateTimeUtil.convertDateStrFormat(lastCollectDate,
								ApplicationConstants.DATETIME_FORMAT_SYSTEM,
								ApplicationConstants.DATETIME_FORMAT_MT);
						if (gameDateFmt.compareTo(lastCollectDateFmt) < 0) {
							// 既に収集済のため収集対象外
							continue;
						}

					}

					// 結果を算出
					if (result.getScore().equals("vs")) {
						break;
					}
					String[] score = result.getScore().split("-");
					int homeScore = Integer.valueOf(score[0]);
					int awayScore = Integer.valueOf(score[1]);
					if (homeScore > awayScore) {
						result.setResult(GameResult.HOME_WIN.getCode());
					} else if (homeScore < awayScore) {
						result.setResult(GameResult.AWAY_WIN.getCode());
					} else {
						result.setResult(GameResult.DRAW.getCode());
					}

				} else if (scrapingType.equals(SCRAPING_TYPE_PROPHET)) {

					if (!result.getScore().equals("vs")) {
						// 試合前以外は対象外
						continue;
					}

					if (!PROPHET_TARGET_LEAGUE.contains(result.getTournament())) {
						// J1、J2、J3以外は対象外
						continue;
					}

					result.setResult(GameResult.BEFORE.getCode());

				}

				resultList.add(result);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return resultList;
	}

	/**
	 * チーム情報のスクレイピング処理を実行する。<br>
	 * J1～J3のチーム情報を順次収集し、スクレイピング結果のサマリを応答する。
	 * 
	 * @return 収集結果
	 */
	@Transactional
	public List<ClubTeamInfo> scrapingClubTeamInfo() {
		List<ClubTeamInfo> clubTeamInfoList = new ArrayList<>();
		String nowDt = DateTimeUtil.getNowDateStr(ApplicationConstants.DATETIME_FORMAT_SYSTEM);

		clubTeamInfoList.addAll(execScrapingTeamInfo(League.J1, nowDt));
		clubTeamInfoList.addAll(execScrapingTeamInfo(League.J2, nowDt));
		clubTeamInfoList.addAll(execScrapingTeamInfo(League.J3, nowDt));

		TtCollectManage lastCollectManage = new TtCollectManage();
		lastCollectManage.setCollectId(ApplicationConstants.LAST_COLLECT_MANAGE_ID_TEAM_INFO);
		lastCollectManage.setCollectName(ApplicationConstants.LAST_COLLECT_MANAGE_NAME_TEAM_INFO);
		lastCollectManage.setLastCollectDate(nowDt);
		collectManageCrudRepository.save(lastCollectManage);

		return clubTeamInfoList;

	}

	/**
	 * MiniTotoの次節対戦カードを取得する
	 * 
	 * @param type くじ種類
	 * @return 対戦カードリスト
	 * @throws Exception
	 */
	public List<MatchingGameInfo> scrapingMiniTotoMatches(String type) throws Exception {
		List<MatchingGameInfo> gameInfoList = new ArrayList<>();
		// MINI TOTO くじ購入はURL直接アクセスができないため、Selniumでアクセスする。
		// セッション切れ対策が必要だが、直近はリトライで回避できているか確認する。
		WebDriver driver = connectWebDriver(TOTO_FORM);
		// ページ描画に時間を要するため、待機する。
		Thread.sleep(5000);

		List<WebElement> dataDivs = driver.findElements(By.className("pl10"));
		WebElement tableElement = null;
		for (WebElement dataDiv : dataDivs) {
			try {
				tableElement = dataDiv.findElement(By.tagName("table"));
			} catch (Exception e) {
				continue;
			}
		}

		List<WebElement> trElements = tableElement.findElements(By.tagName("tr"));
		// 対戦内容記載行
		int matchOrderRow = 2;
		// TypeA記載行
		int targetRowTypeA = 4;
		// TypeB記載行
		int targetRowTypeB = 5;
		
		int targetRow = 0;
		if (type.equals("typeA")) {
			targetRow = targetRowTypeA;
		} else if (type.equals("typeB")) {
			targetRow = targetRowTypeB;
		}

		Map<Integer, String> matchOrderMap = new LinkedHashMap<>();
		String gameDate = null;

		for (int i = 0; i < trElements.size(); i++) {
			if (matchOrderRow == i) {
				WebElement trElement = trElements.get(i);
				List<WebElement> thElements = trElement.findElements(By.tagName("th"));
				for (int j = 0; j < thElements.size(); j++) {
					WebElement thElemet = thElements.get(j);
					String matchingStr = thElemet.getText().trim().replaceAll("\\s+", "").replaceAll("\\n", "");
					matchOrderMap.put(j, matchingStr);
				}
			}

			if (targetRow == i) {
				WebElement trElement = trElements.get(i);
				List<WebElement> tdElements = trElement.findElements(By.tagName("td"));

				for (int k = 0; k < tdElements.size(); k++) {
					if (k < 3)
						continue;
					WebElement tdElement = tdElements.get(k);
					if (k == 3) {
						gameDate = tdElement.getText();
					} else if (tdElement.getText().equals("○")) {
						MatchingGameInfo gameInfo = new MatchingGameInfo();
						gameInfo.setDate(gameDate);
						String matching = matchOrderMap.get(k - 4);
						String[] matchingArray = matching.split("VS");
						gameInfo.setHome(matchingArray[0]);
						gameInfo.setAway(matchingArray[1]);
						gameInfoList.add(gameInfo);
					}
				}
			}

		}
		return gameInfoList;
	}

	/**
	 * チーム情報のスクレイピングを実行する。
	 * 
	 * @param league リーグ種別
	 * @param nowDt 現在日時
	 * @return 収集結果
	 */
	private List<ClubTeamInfo> execScrapingTeamInfo(League league, String nowDt) {
		List<ClubTeamInfo> clubTeamInfoList = new ArrayList<>();
		String url = null;
		if (League.J1 == league) {
			url = J_LEAGUE_CLUB_TEAM_URL_J1;
		} else if (League.J2 == league) {
			url = J_LEAGUE_CLUB_TEAM_URL_J2;
		} else if (League.J3 == league) {
			url = J_LEAGUE_CLUB_TEAM_URL_J3;
		} else {
			throw new RuntimeException("予期しないリーグが指定されました! リーグ名: " + league.getName());
		}

		try {
			Document doc = getJsoupResult(url);
			Element contentMain = doc.getElementsByClass("tab-contents-main").first();
			Elements mainBoxes = contentMain.getElementsByClass("main-box-base");

			for (Element mainBox : mainBoxes) {
				// Home and Awayで2件セット
				List<ClubTeamInfo> clubTeamInfoHomeAndAway = new ArrayList<>();
				clubTeamInfoHomeAndAway.add(new ClubTeamInfo());
				clubTeamInfoHomeAndAway.add(new ClubTeamInfo());

				Elements imges = mainBox.getElementsByClass("img");

				for (int j = 0; j < 2; j++) {
					Element image = imges.get(j);
					ClubTeamInfo clubTeamInfo = clubTeamInfoHomeAndAway.get(j);

					Element img = image.select("img").first();
					String teamShortName = img.attr("alt");
					clubTeamInfo.setTeamShortName(teamShortName);

					Element detailAnchor = image.select("a").first();
					String detailLink = detailAnchor.attr("href");
					clubTeamInfo.setTeamLink(detailLink);
				}

				clubTeamInfoList.addAll(clubTeamInfoHomeAndAway);
			}

			for (ClubTeamInfo clubTeamInfo : clubTeamInfoList) {
				Document docDetail = getJsoupResult(clubTeamInfo.getTeamLink());
				String teamName = docDetail.select("h3").first().text();
				// サイト仕様でTeam名が二重に取得されるため、文字列加工
				teamName = teamName.substring(0, teamName.length() / 2);
				clubTeamInfo.setTeamName(teamName);
				clubTeamInfo.setRate(ApplicationConstants.DEFAULT_RATE);
				clubTeamInfo.setLeague(league.getName());
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("スクレイピング処理に失敗しました! リーグ名: " + league.getName());
		}

		teamCustomRepository.saveTeam(clubTeamInfoList, league, nowDt);
		return clubTeamInfoList;

	}

	/**
	 * 指定したURLから情報を取得する。<br>
	 * 通信失敗時はリトライ回数上限まで繰り返す。<br>
	 * 上限に達した場合は例外をThrowする。
	 * 
	 * @param url スクレイピング対象のURL
	 * @return URL読取結果
	 * @throws Exception 例外が発生した場合
	 */
	private Document getJsoupResult(String url) throws Exception {
		int retry = 0;
		while (true) {
			try {
				return Jsoup.connect(url).get();

			} catch (SocketTimeoutException se) {
				if (retry < MAX_RETRY_COUNT) {
					retry++;
					continue;
				}

				throw se;
			}
		}
	}

	/**
	 * SelniumのWebDriverを利用して接続
	 * @param url 接続対象
	 * @return WebDriver
	 * @throws InterruptedException
	 */
	private WebDriver connectWebDriver(String url) throws InterruptedException {
		int retry = 0;
		while (true) {
			try {
				WebDriver driver = generateWebDriver();
				driver.get(url); //対象サイトが読込完了するまでSelniumが待機
				return driver;
			} catch (Exception e) {
				retry++;
				if (retry == MAX_RETRY_COUNT) {
					logger.error("スクレイピング対象の接続に失敗しました。 URL:{}", url);
					return null;
				}
				Thread.sleep(1000);
				logger.warn("接続失敗のためリトライします。リトライ{}回目, URL:{}\"", retry, url);
				continue;
			}
		}
	}

	/**
	 * WebDriverを生成する
	 * @return WebDriver
	 * @throws MalformedURLException 
	 */
	private WebDriver generateWebDriver() throws MalformedURLException {
		WebDriverManager.chromedriver().setup();
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--remote-allow-origins=*", "--window-size=1920,1080", "-ignore-certificate-errors",
				"--headless",
				"user-agenet=" + getRandomUserAgent());
		return new ChromeDriver(options);
	}

	/**
	 * ランダムなユーザーエージェントを生成する
	 * @return ユーザーエージェント
	 */
	private String getRandomUserAgent() {
		String[] userAgents = {
				"Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36",
				"Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; AS; rv:11.0) like Gecko",
				"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.246",
				"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.101 Safari/537.36" };
		int randomNumber = new Random().nextInt(userAgents.length);
		return userAgents[randomNumber];
	}

}