package com.example.springbatchtest.config;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyBatisBatchConfig {

    @Bean
    public SqlSessionTemplate batchSqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        // ★ 배치 실행용
        return new SqlSessionTemplate(sqlSessionFactory, ExecutorType.BATCH);
    }
}
