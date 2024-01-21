package jp.co.mini.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import jp.co.mini.model.table.TtHistogram;

/**
 * ヒストグラム情報リポジトリインタフェース
 */
public interface HistogramRepositoryArchive extends JpaRepository<TtHistogram, Integer> {

}
