package com.example.springbatchtest.batch.file.importfile;


import com.example.springbatchtest.dto.UserTx;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableBatchProcessing
@MapperScan(basePackages = "com.example.springbatchtest.batch.mapper")
public class FileToDbMyBatisJobConfig {

    private final JobBuilderFactory jobs;
    private final StepBuilderFactory steps;

    @Bean
    public Step fileToDbStep(FlatFileItemReader<UserTx> userTxReader,
                             ItemProcessor<UserTx, UserTx> userTxProcessorBean,
                             UserTxEnrichingMyBatisWriter userTxEnrichingMyBatisWriter) {

        return steps.get("fileToDbStep")
                .<UserTx, UserTx>chunk(10)
                .reader(userTxReader)
                .processor(userTxProcessorBean)
                .writer(userTxEnrichingMyBatisWriter)
                .faultTolerant()
                .skipLimit(100)// 최대 100번까지 skip 허용 (item 기준)
                .skip(IllegalArgumentException.class) // 건너뛸 예외 유형 지정
                .retryLimit(3)
                .retry(DataAccessException.class) // 재시도할 예외 유형 지정
                .build();
    }

    @Bean
    public Job fileToDbJob(Step fileToDbStep) {
        return jobs.get("fileToDbJob")
                .incrementer(new RunIdIncrementer())
                .start(fileToDbStep)
                .build();
    }
}