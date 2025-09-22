package com.example.springbatchtest.batch.file.exportfile;


import com.example.springbatchtest.dto.UserTx;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisPagingItemReader;
import org.mybatis.spring.batch.builder.MyBatisPagingItemReaderBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class DbToFileJobConfig {

    private final JobBuilderFactory jobs;
    private final StepBuilderFactory steps;

    @Bean
    public MyBatisPagingItemReader<UserTx> userTxDbReader(SqlSessionFactory sqlSessionFactory) {
        return new MyBatisPagingItemReaderBuilder<UserTx>()
                .sqlSessionFactory(sqlSessionFactory)
                .queryId("com.example.springbatchtest.batch.mapper.TxAggMapper.selectUserTxPage")
                .pageSize(100)
                .build();
    }

    @Bean
    @StepScope //파라미터 초기화를 위해서 필요
    public FlatFileItemWriter<UserTx> userTxFileWriter(
            @Value("#{jobParameters['outputFile']}") String outputFile) {

        return new FlatFileItemWriterBuilder<UserTx>()
                .name("userTxFileWriter")
                .resource(new FileSystemResource(outputFile))
                .encoding("UTF-8")
                .lineAggregator(item -> String.join(" ",
                        item.getTxId(),
                        item.getUserId(),
                        item.getProductCode(),
                        item.getAmount().toPlainString(),
                        item.getTxDate().toString()
                ))
                .build();
    }


    @Bean
    public Step dbToFileStep(MyBatisPagingItemReader<UserTx> userTxDbReader,
                             FlatFileItemWriter<UserTx> userTxFileWriter) {
        return steps.get("dbToFileStep")
                .<UserTx, UserTx>chunk(100)
                .reader(userTxDbReader)
                .writer(userTxFileWriter)
                .build();
    }

    @Bean
    public Job dbToFileJob(Step dbToFileStep) {
        return jobs.get("dbToFileJob")
                .incrementer(new RunIdIncrementer())
                .start(dbToFileStep)
                .build();
    }
}
