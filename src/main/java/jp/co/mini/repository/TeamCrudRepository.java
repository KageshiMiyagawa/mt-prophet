package jp.co.mini.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import jp.co.mini.model.table.TtTeam;

/**
 * チーム情報CRUDリポジトリインタフェース
 */
public interface TeamCrudRepository extends JpaRepository<TtTeam, Integer> {

	/**
	 * チーム略称を抽出する。<br>
	 * select * from tt_team where team_short_name in ({teamShortName});
	 * 
	 * @param teamShortName チーム略称
	 * @return チーム情報
	 */
	List<TtTeam> findByTeamShortNameIn(List<String> teamShortName);
}
