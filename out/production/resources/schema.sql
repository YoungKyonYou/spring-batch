drop table if exists monthly_tx_agg cascade;
drop table if exists daily_tx_agg cascade;
drop table if exists tx cascade;

-- 거래 원천 테이블
create table if not exists tx (
  id        bigserial primary key,
  tx_date   date        not null,
  amount    numeric(18,2) not null
);

create index if not exists idx_tx_tx_date on tx(tx_date);

-- 일 집계 테이블
create table if not exists daily_tx_agg (
  tx_date      date primary key,
  total_amount numeric(18,2) not null,
  tx_count     integer       not null
);

-- 월 집계 테이블 (해당 월의 1일을 키로 사용)
create table if not exists monthly_tx_agg (
  ym_date      date primary key,   -- ex) 2025-09-01
  total_amount numeric(18,2) not null,
  tx_count     integer       not null
);