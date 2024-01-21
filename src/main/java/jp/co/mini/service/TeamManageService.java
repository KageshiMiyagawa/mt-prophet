package jp.co.mini.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.mini.ApplicationConstants;
import jp.co.mini.model.table.TtTeam;
import jp.co.mini.repository.TeamCrudRepository;
import jp.co.mini.repository.TeamCustomRepository;
import jp.co.mini.util.DateTimeUtil;

/**
 * チーム管理サービスクラス
 */
@Service
public class TeamManageService {
	
	@Autowired
	private TeamCustomRepository teamCustomRepository;
	@Autowired
	private TeamCrudRepository teamCrudRepository;
	
	private static final String UNKNOWN_PREFIX = "(公式未登録)";
	
	/**
	 * 未登録のチームを初期レートで登録する
	 * @param unknownTeamNameList 未登録チームリスト
	 */
	@Transactional
	public void saveUnknownTeam(List<String> unknownTeamNameList) {
		
		List<TtTeam> teamList = new ArrayList<>();
		int teamId = teamCustomRepository.getTeamId();
		String nowDt = DateTimeUtil.getNowDateStr(ApplicationConstants.DATETIME_FORMAT_SYSTEM);
		
		for (String unknownTeam : unknownTeamNameList) {
			TtTeam team = new TtTeam();
			team.setTeamId(teamId);
			team.setTeamName(unknownTeam + UNKNOWN_PREFIX);
			team.setTeamShortName(unknownTeam);
			team.setLeague(UNKNOWN_PREFIX);
			team.setHomeRate(ApplicationConstants.DEFAULT_RATE);
			team.setAwayRate(ApplicationConstants.DEFAULT_RATE);
			team.setTeamLink(UNKNOWN_PREFIX);
			team.setRegistDate(nowDt);
			team.setUpdateDate(nowDt);
			teamList.add(team);
			
			teamId ++;
		}
		
		teamCrudRepository.saveAll (teamList);
	}
}