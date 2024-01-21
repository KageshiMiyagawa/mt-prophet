package jp.co.mini.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import jp.co.mini.model.table.TmHistogram;

/**
 * ヒストグラム情報リポジトリインタフェース
 */
public interface HistogramRepository extends JpaRepository<TmHistogram, Integer> {

}
