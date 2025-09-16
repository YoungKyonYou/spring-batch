package com.example.springbatchtest.config;

import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchJobConfig {



//    @Bean
//    public Job sampleJob() {
//        return jobBuilderFactory.get("sampleJob")
//                .start(sampleStep())
//                .build();
//    }
//
//    @Bean
//    public Step sampleStep() {
//        return stepBuilderFactory.get("sampleStep")
//                .tasklet((contribution, chunkContext) -> {
//                    log.info(">>>>> Hello Spring Batch (just printing)");
//                    return org.springframework.batch.repeat.RepeatStatus.FINISHED;
//                })
//                .build();
//    }
    @Primary
    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(DataSource ds) {
        return new NamedParameterJdbcTemplate(ds);
    }
}
