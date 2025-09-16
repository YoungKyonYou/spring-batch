package com.example.springbatchtest.batch.job;

import com.example.springbatchtest.dto.DayAgg;
import com.example.springbatchtest.dto.MonthAgg;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class TxDailyMonthlyJobConfig {

    private final JobBuilderFactory jobs;
    private final StepBuilderFactory steps;
    private final DataSource dataSource;

    public TxDailyMonthlyJobConfig(JobBuilderFactory jobs, StepBuilderFactory steps, DataSource dataSource) {
        this.jobs = jobs;
        this.steps = steps;
        this.dataSource = dataSource;
    }

    // Step1 일 집계 Reader
    @Bean
    @StepScope
    public JdbcPagingItemReader<DayAgg> dayAggReader(
            DataSource dataSource,
            @Value("#{jobParameters['from']}") String from,
            @Value("#{jobParameters['to']}") String to) {

        StringBuilder sb = new StringBuilder();
        sb.append(" from (").append("\n")
                .append("   select CAST(tx_date AS DATE) as tx_date").append("\n")
                .append("        , sum(amount) as total_amount").append("\n")
                .append("        , count(*)   as tx_count").append("\n")
                .append("   from tx").append("\n")
                .append("   where 1=1");

        Map<String, Object> paramMap = new HashMap<>();
        if (from != null) {
            sb.append(" and tx_date >= CAST(:from AS DATE)");
            paramMap.put("from", from);
        }
        if (to != null) {
            sb.append(" and tx_date < DATEADD('DAY', 1, CAST(:to AS DATE))");
            paramMap.put("to", to);
        }

        sb.append("   group by CAST(tx_date AS DATE)").append("\n")
                .append(" ) t");

        return new JdbcPagingItemReaderBuilder<DayAgg>()
                .name("dayAggReader")
                .dataSource(dataSource)
                .selectClause("select tx_date, total_amount, tx_count")
                .fromClause(sb.toString())
                .sortKeys(Map.of("tx_date", Order.ASCENDING))
                .parameterValues(paramMap)
                .rowMapper((ResultSet rs, int rowNum) -> new DayAgg(
                        rs.getDate("tx_date").toLocalDate(),
                        rs.getBigDecimal("total_amount"),
                        rs.getInt("tx_count")
                ))
                .pageSize(100)
                .build();
    }


    // Step1 Processor - 선택적인 부분 예시로 넣어둠
    @Bean
    public ItemProcessor<DayAgg, DayAgg> dayAggProcessor() {
        return new ItemProcessor<>() {

            @Override
            public DayAgg process(DayAgg item) {
                // 검증 로직
                if (item.getTxCount() <= 0) {
                    log.warn("잘못된 집계: {}, count={}", item.getTxDate(), item.getTxCount());
                    return null; // null 리턴 시 해당 아이템은 Writer로 전달되지 않음
                }
                if (item.getTotalAmount().signum() < 0) {
                    log.warn("음수 금액 집계: {}, amount={}", item.getTxDate(), item.getTotalAmount());
                    return null;
                }

                log.debug("유효 데이터: {}", item);
                return item; // 그대로 전달
            }
        };
    }

    // Step1 Writer
    @Bean
    public ItemWriter<DayAgg> dayAggWriter() {
        StringBuilder sql = new StringBuilder();
        sql.append("merge into daily_tx_agg (tx_date, total_amount, tx_count)").append("\n")
                .append("key(tx_date)").append("\n")
                .append("values (:txDate, :totalAmount, :txCount)");


        return new JdbcBatchItemWriterBuilder<DayAgg>()
                .dataSource(dataSource)
                .sql(sql.toString())
                .beanMapped() //파라미터 dto 매핑
                .build();
    }

    // Step1 구성
    @Bean
    public Step dailyAggregateStep(JdbcPagingItemReader<DayAgg> dayAggReader,
                                   ItemProcessor<DayAgg, DayAgg> dayAggProcessor) {
        return steps.get("dailyAggregateStep")
                .<DayAgg, DayAgg>chunk(100)
                .reader(dayAggReader)
                .processor(dayAggProcessor)
                .writer(dayAggWriter())
                .faultTolerant()
                .retryLimit(3)
                .retry(Exception.class)
                .build();
    }

    // Step2 월 집계 Reader
    @Bean
    @StepScope
    public JdbcPagingItemReader<MonthAgg> monthAggReader(
            @Value("#{jobParameters['from']}") String from,
            @Value("#{jobParameters['to']}") String to) {

        StringBuilder sb = new StringBuilder();
        sb.append(" from (").append("\n")
                .append("   select ").append("\n")
                .append("          DATEADD('DAY', 1 - DAYOFMONTH(tx_date), tx_date) as ym_date").append("\n")
                .append("        , sum(total_amount) as total_amount").append("\n")
                .append("        , sum(tx_count)     as tx_count").append("\n")
                .append("   from daily_tx_agg").append("\n")
                .append("   where 1=1");

        Map<String, Object> paramMap = new HashMap<>();
        if (from != null) {
            sb.append(" and tx_date >= CAST(:from AS DATE)");
            paramMap.put("from", from);
        }
        if (to != null) {
            sb.append(" and tx_date < DATEADD('DAY', 1, CAST(:to AS DATE))");
            paramMap.put("to", to);
        }

        sb.append("   group by DATEADD('DAY', 1 - DAYOFMONTH(tx_date), tx_date)").append("\n")
                .append(" ) t");

        return new JdbcPagingItemReaderBuilder<MonthAgg>()
                .name("monthAggReader")
                .dataSource(dataSource)
                .selectClause("select ym_date, total_amount, tx_count")
                .fromClause(sb.toString())
                .sortKeys(Map.of("ym_date", Order.ASCENDING))
                .parameterValues(paramMap)
                .rowMapper((rs, i) -> new MonthAgg(
                        rs.getDate("ym_date").toLocalDate(),
                        rs.getBigDecimal("total_amount"),
                        rs.getInt("tx_count")
                ))
                .pageSize(100)
                .build();
    }

    // Step2 Writer
    @Bean
    public ItemWriter<MonthAgg> monthAggWriter() {
        StringBuilder sql = new StringBuilder();
        sql.append("merge into monthly_tx_agg (ym_date, total_amount, tx_count)").append("\n")
                .append("key(ym_date)").append("\n")
                .append("values (:ymDate, :totalAmount, :txCount)");

        return new JdbcBatchItemWriterBuilder<MonthAgg>()
                .dataSource(dataSource)
                .sql(sql.toString())
                .beanMapped()
                .build();
    }

    // Step 2 구성
    @Bean
    public Step monthlyAggregateStep(JdbcPagingItemReader<MonthAgg> monthAggReader) {
        return steps.get("monthlyAggregateStep")
                .<MonthAgg, MonthAgg>chunk(100)
                .reader(monthAggReader)
                .writer(monthAggWriter())
                .faultTolerant()
                .retryLimit(3)
                .retry(Exception.class)
                .build();
    }

    // Job 구성
    @Bean
    public Job txDailyMonthlyJob(Step dailyAggregateStep, Step monthlyAggregateStep) {
        return jobs.get("txDailyMonthlyJob")
                .incrementer(new RunIdIncrementer())
                .start(dailyAggregateStep)
                .next(monthlyAggregateStep)
                .build();
    }


}
