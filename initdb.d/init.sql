CREATE TABLE mtprophet.tt_game_result(
    game_id INTEGER NOT NULL COMMENT '試合ID',
    year VARCHAR(32) NOT NULL COMMENT '年度',
    tournament VARCHAR(32) NOT NULL COMMENT '大会',
    section VARCHAR(32) COMMENT '節',
    game_date VARCHAR(32) COMMENT '試合日',
    game_time VARCHAR(32) NOT NULL COMMENT 'K/O時刻',
    home VARCHAR(32) COMMENT 'ホーム',
    score VARCHAR(32) COMMENT 'スコア',
    away VARCHAR(32) NOT NULL COMMENT 'アウェイ',
    stadium VARCHAR(32) NOT NULL COMMENT 'スタジアム',
    result CHAR(1) COMMENT '試合結果',
    home_rate INTEGER comment 'ホームチームレーティング',
    away_rate INTEGER comment 'アウェイチームレーティング',
    calc_flag CHAR(1) COMMENT '計算済フラグ',
    histogram_flag CHAR(1) COMMENT 'ヒストグラム作成済フラグ',
	regist_date CHAR(14) NOT NULL  COMMENT '登録日時',
	update_date CHAR(14) NOT NULL  COMMENT '更新日時',
    PRIMARY KEY (game_id)
) COMMENT '試合結果';

CREATE TABLE mtprophet.tt_team (
    team_id INTEGER NOT NULL PRIMARY KEY COMMENT 'チームID',
    team_name VARCHAR(32) NOT NULL  COMMENT 'チーム名',
    team_short_name VARCHAR(32) NOT NULL  COMMENT 'チーム略称',
    league VARCHAR(32) NOT NULL  COMMENT '所属リーグ',
	home_rate INTEGER comment 'ホームレーティング',
	away_rate INTEGER comment 'アウェイレーティング',
    team_link TEXT COMMENT 'チーム詳細リンク',
	regist_date CHAR(14) NOT NULL  COMMENT '登録日時',
	update_date CHAR(14) NOT NULL  COMMENT '更新日時'
) COMMENT='チーム';

CREATE TABLE mtprophet.tt_collect_manage (
	collect_id INTEGER NOT NULL PRIMARY KEY COMMENT '収集ID',
	collect_name VARCHAR(32) NOT NULL COMMENT '収集名',
    last_collect_date CHAR(14) NOT NULL  COMMENT '最終収集日時'
) COMMENT='収集管理';

CREATE TABLE mtprophet.tt_histogram (
	record_number INTEGER NOT NULL PRIMARY KEY COMMENT 'レコードNo',
	win_count INTEGER NOT NULL COMMENT '勝利数',
	lose_count INTEGER NOT NULL COMMENT '敗北数',
	draw_count INTEGER NOT NULL COMMENT '引き分け数',
	rate_width INTEGER NOT NULL COMMENT 'レート差',
	description VARCHAR(32) NOT NULL COMMENT '説明'
) COMMENT='ヒストグラム';

CREATE TABLE mtprophet.tm_histogram (
	record_number INTEGER NOT NULL PRIMARY KEY COMMENT 'レコードNo',
	win_percent double NOT NULL COMMENT '勝率',
	lose_percent double NOT NULL COMMENT '敗率',
	draw_percent double NOT NULL COMMENT '引分率',
	rate_width INTEGER NOT NULL COMMENT 'レート差',
	description VARCHAR(32) NOT NULL COMMENT '説明'
) COMMENT='ヒストグラムマスタ';

-- 初期データ
INSERT INTO mtprophet.tt_histogram VALUES (1,0,0,0,50,'0～50');
INSERT INTO mtprophet.tt_histogram VALUES (2,0,0,0,100,'51～100');
INSERT INTO mtprophet.tt_histogram VALUES (3,0,0,0,150,'101～150');
INSERT INTO mtprophet.tt_histogram VALUES (4,0,0,0,200,'151～200');
INSERT INTO mtprophet.tt_histogram VALUES (5,0,0,0,250,'201～250');
INSERT INTO mtprophet.tt_histogram VALUES (6,0,0,0,300,'251～300');
INSERT INTO mtprophet.tt_histogram VALUES (7,0,0,0,350,'301～350');
INSERT INTO mtprophet.tt_histogram VALUES (8,0,0,0,400,'351～400');
INSERT INTO mtprophet.tt_histogram VALUES (9,0,0,0,450,'401～450');
INSERT INTO mtprophet.tt_histogram VALUES (10,0,0,0,500,'451～500');
INSERT INTO mtprophet.tt_histogram VALUES (11,0,0,0,999999,'501～');

-- 初期データ（ヒストグラムマスタ）
INSERT INTO mtprophet.tm_histogram VALUES (1,40.0,35.0,25.0,50,'0～50');
INSERT INTO mtprophet.tm_histogram VALUES (2,43.5,31.5,25.0,100,'51～100');
INSERT INTO mtprophet.tm_histogram VALUES (3,47.5,28.5,24.0,150,'101～150');
INSERT INTO mtprophet.tm_histogram VALUES (4,51.5,25.5,23.0,200,'151～200');
INSERT INTO mtprophet.tm_histogram VALUES (5,54.0,22.0,24.0,250,'201～250');
INSERT INTO mtprophet.tm_histogram VALUES (6,56.5,20.0,23.5,300,'251～300');
INSERT INTO mtprophet.tm_histogram VALUES (7,58.0,18.0,24.0,350,'301～350');
INSERT INTO mtprophet.tm_histogram VALUES (8,60.5,16.0,23.5,400,'351～400');
INSERT INTO mtprophet.tm_histogram VALUES (9,62.5,14.0,23.5,450,'401～450');
INSERT INTO mtprophet.tm_histogram VALUES (10,64.0,12.5,23.5,500,'451～500');
INSERT INTO mtprophet.tm_histogram VALUES (11,66.5,10.0,23.5,999999,'501～');