package com.example.springbatchtest.batch.job;

import com.example.springbatchtest.dto.DayAgg;
import com.example.springbatchtest.dto.MonthAgg;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.batch.MyBatisBatchItemWriter;
import org.mybatis.spring.batch.MyBatisPagingItemReader;
import org.mybatis.spring.batch.builder.MyBatisBatchItemWriterBuilder;
import org.mybatis.spring.batch.builder.MyBatisPagingItemReaderBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableBatchProcessing
@MapperScan(basePackages = "com.example.springbatchtest.mapper")
public class TxDailyMonthlyJobConfig {

    private final JobBuilderFactory jobs;
    private final StepBuilderFactory steps;

    public TxDailyMonthlyJobConfig(JobBuilderFactory jobs, StepBuilderFactory steps) {
        this.jobs = jobs;
        this.steps = steps;
    }

    @Bean
    @StepScope
    public MyBatisPagingItemReader<DayAgg> dayAggReader(
            SqlSessionFactory sqlSessionFactory,
            @Value("#{jobParameters['from']}") String from,
            @Value("#{jobParameters['to']}")   String to) {

        Map<String, Object> params = new HashMap<>();
        params.put("from", from);
        params.put("to",   to);

        return new MyBatisPagingItemReaderBuilder<DayAgg>()
                .sqlSessionFactory(sqlSessionFactory)
                .queryId("com.example.springbatchtest.mapper.TxAggMapper.selectDayAggPage")
                .parameterValues(params)
                .pageSize(100)
                .build();
    }

    @Bean
    public ItemProcessor<DayAgg, DayAgg> dayAggProcessor() {
        return item -> {
            if (item.getTxCount() <= 0) {
                log.warn("잘못된 집계: {}, count={}", item.getTxDate(), item.getTxCount());
                return null; // null → writer로 전달되지 않음
            }
            if (item.getTotalAmount().signum() < 0) {
                log.warn("음수 금액 집계: {}, amount={}", item.getTxDate(), item.getTotalAmount());
                return null;
            }
            log.debug("유효 데이터: {}", item);
            return item;
        };
    }

    @Bean
    public MyBatisBatchItemWriter<DayAgg> dayAggWriter(SqlSessionFactory sqlSessionFactory) {
        return new MyBatisBatchItemWriterBuilder<DayAgg>()
                .sqlSessionFactory(sqlSessionFactory)
                .statementId("com.example.springbatchtest.mapper.TxAggMapper.upsertDailyAgg")
                .build();
    }

    @Bean
    public Step dailyAggregateStep(MyBatisPagingItemReader<DayAgg> dayAggReader,
                                   ItemProcessor<DayAgg, DayAgg> dayAggProcessor,
                                   MyBatisBatchItemWriter<DayAgg> dayAggWriter) {
        return steps.get("dailyAggregateStep")
                .<DayAgg, DayAgg>chunk(100)
                .reader(dayAggReader)
                .processor(dayAggProcessor)
                .writer(dayAggWriter)
                .faultTolerant()
                .retryLimit(3)
                .retry(Exception.class)
                .build();
    }

    @Bean
    @StepScope
    public MyBatisPagingItemReader<MonthAgg> monthAggReader(
            SqlSessionFactory sqlSessionFactory,
            @Value("#{jobParameters['from']}") String from,
            @Value("#{jobParameters['to']}")   String to) {

        Map<String, Object> params = new HashMap<>();
        params.put("from", from);
        params.put("to",   to);

        return new MyBatisPagingItemReaderBuilder<MonthAgg>()
                .sqlSessionFactory(sqlSessionFactory)
                .queryId("com.example.springbatchtest.mapper.TxAggMapper.selectMonthAggPage")
                .parameterValues(params)
                .pageSize(100)
                .build();
    }

    @Bean
    public MyBatisBatchItemWriter<MonthAgg> monthAggWriter(SqlSessionFactory sqlSessionFactory) {
        return new MyBatisBatchItemWriterBuilder<MonthAgg>()
                .sqlSessionFactory(sqlSessionFactory)
                .statementId("com.example.springbatchtest.mapper.TxAggMapper.upsertMonthlyAgg")
                .build();
    }

    @Bean
    public Step monthlyAggregateStep(MyBatisPagingItemReader<MonthAgg> monthAggReader,
                                     MyBatisBatchItemWriter<MonthAgg> monthAggWriter) {
        return steps.get("monthlyAggregateStep")
                .<MonthAgg, MonthAgg>chunk(100)
                .reader(monthAggReader)
                .writer(monthAggWriter)
                .faultTolerant() //예외를 허용하고 계속 진행할 수 있는 모드
                .retryLimit(3)
                .retry(Exception.class)
                .build();
    }

    @Bean
    public Job txDailyMonthlyJob(Step dailyAggregateStep, Step monthlyAggregateStep) {
        return jobs.get("txDailyMonthlyJob")
                .incrementer(new RunIdIncrementer())
                .start(dailyAggregateStep)
                .next(monthlyAggregateStep)
                .build();
    }
}