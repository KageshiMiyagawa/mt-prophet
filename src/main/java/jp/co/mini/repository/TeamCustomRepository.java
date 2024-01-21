package jp.co.mini.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jp.co.mini.ApplicationConstants;
import jp.co.mini.code.League;
import jp.co.mini.model.ClubTeamInfo;
import jp.co.mini.model.table.TtTeam;

/**
 * チームカスタムリポジトリ
 */
@Repository
public class TeamCustomRepository {

	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private TeamCrudRepository teamCrudRepository;

	/**
	 * チーム情報を登録する
	 * 
	 * @param scrapingClubTeamInfoList チーム情報リスト
	 * @param league リーグ
	 * @param nowDt 現在日時
	 */
	public void saveTeam(List<ClubTeamInfo> scrapingClubTeamInfoList, League league, String nowDt) {
		List<TtTeam> teamList = new ArrayList<>();
		int teamId = getTeamId();
		for(ClubTeamInfo scrapingClubTeamInfo : scrapingClubTeamInfoList) {
			TtTeam team = new TtTeam();
			BeanUtils.copyProperties(scrapingClubTeamInfo, team);
			team.setTeamId(teamId);
			team.setLeague(league.getName());
			team.setHomeRate(ApplicationConstants.DEFAULT_RATE);
			team.setAwayRate(ApplicationConstants.DEFAULT_RATE);
			team.setRegistDate(nowDt);
			team.setUpdateDate(nowDt);
			teamList.add(team);
			
			teamId ++;
		}
		
		teamCrudRepository.saveAll (teamList);
	}
	
	/**
	 * 登録するチームIDを取得する
	 * @return チームID
	 */
	public int getTeamId() {
		Object maxTeamId = entityManager.createNativeQuery("SELECT MAX(team_id) FROM tt_team;").getSingleResult();
		if (Objects.isNull(maxTeamId)) {
			return 1;
		}
		return (int) maxTeamId + 1;
	}
}
