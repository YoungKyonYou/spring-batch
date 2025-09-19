package com.example.springbatchtest.batch.job;

import com.example.springbatchtest.dto.DayAgg;
import com.example.springbatchtest.dto.MonthAgg;
import com.example.springbatchtest.service.MonthService;
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
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@MapperScan(basePackages = "com.example.springbatchtest.mapper")
public class TxDaily {

    public static final String JOB_NAME = "B000_txDailyMonthlyJob";
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final SqlSessionFactory sqlSessionFactory;
    private final MonthService monthService;
//    private final TxAggMapper txAggMapper;


    public TxDaily(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, SqlSessionFactory sqlSessionFactory, MonthService monthService) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.sqlSessionFactory = sqlSessionFactory;
        this.monthService = monthService;
    }

    @Bean("txDailyMonthlyJob2")
    public Job txDailyMonthlyJob(Step dailyAggregateStep) {
        return jobBuilderFactory.get(JOB_NAME)
                .incrementer(new RunIdIncrementer())
                .start(dailyAggregateStep)
                .build();
    }

    // Step1 일집계 step에 대한 Configuration 작업
    @Bean("dayAggReader2")
    public Step dailyAggregateStep(MyBatisPagingItemReader<DayAgg> dayAggReader,
                                   ItemProcessor<DayAgg, DayAgg> dayAggProcessor,
                                   MyBatisBatchItemWriter<DayAgg> dayAggWriter) {
        return this.stepBuilderFactory.get("dailyAggregateStep")
                .<DayAgg, DayAgg>chunk(100)
                .reader(dayAggReader)
                .processor(dayAggProcessor)
                .writer(dayAggWriter)
                .faultTolerant()
                .retryLimit(3)
                .retry(Exception.class)
                .build();
    }

    @Bean("dayAggReader2")
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

    @Bean("dayAggProcessor2")
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
    @Bean("dayAggWriter2")
    public ItemWriter<DayAgg> dayAggWriter() {
        MyBatisBatchItemWriter<DayAgg> delegate = new MyBatisBatchItemWriterBuilder<DayAgg>()
                .sqlSessionFactory(sqlSessionFactory)
                .statementId("com.example.springbatchtest.mapper.TxAggMapper.upsertDailyAgg")
                .build();

        return items -> {

            List<DayAgg> list = new ArrayList<>();
            for (DayAgg d : items) {
                MonthAgg mayAgg = monthService.selectMonthAgg(d);
                BigDecimal amt = d.getTotalAmount();
                if (amt.compareTo(BigDecimal.ZERO) < 0) {
                    continue;
                }
                list.add(d);
            }
            delegate.write(list);
        };
    }
}