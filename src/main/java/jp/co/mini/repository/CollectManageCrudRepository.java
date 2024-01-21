package jp.co.mini.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import jp.co.mini.model.table.TtCollectManage;

/**
 * 最終収集日時CRUDリポジトリインタフェース
 */
public interface CollectManageCrudRepository extends JpaRepository<TtCollectManage, Integer> {

}
