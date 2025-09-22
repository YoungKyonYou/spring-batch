drop table if exists monthly_tx_agg cascade;
drop table if exists daily_tx_agg cascade;
drop table if exists tx cascade;
drop table if exists user_dim cascade;
drop table if exists product_dim cascade;
drop table if exists user_tx cascade;

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

-- 사용자 차원 테이블
create table if not exists user_dim (
    user_id  varchar(64) primary key,
    user_pk  bigserial unique
);

-- 상품 차원 테이블
create table if not exists product_dim (
    product_code varchar(32) primary key,
    product_pk   bigserial unique
);

-- 거래 사실 테이블 (업서트 타깃)
create table if not exists user_tx (
    tx_id        varchar(64) primary key,
    user_id      varchar(64) not null references user_dim(user_id),
    product_code varchar(32) not null references product_dim(product_code),
    amount       numeric(18,2) not null check (amount >= 0),
    tx_date      date not null
);