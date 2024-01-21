package jp.co.mini.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import jp.co.mini.model.table.TtGameResult;

/**
 * 試合結果CRUDリポジトリインタフェース
 */
public interface GameResultCrudRepository extends JpaRepository<TtGameResult, Integer> {

	List<TtGameResult> findByCalcFlag(String calcFlag);
}
