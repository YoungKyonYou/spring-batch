-- 샘플 데이터 1,000건: 2025-08-01 ~ 2025-09-30 사이로 분포
insert into tx (tx_date, amount)
select (date '2025-08-01' + (g % 61))::date,
        round((random() * 9000 + 1000)::numeric, 2)
from generate_series(1, 1000) as s(g);
