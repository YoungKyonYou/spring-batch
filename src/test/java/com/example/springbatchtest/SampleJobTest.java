/*
package com.example.springbatchtest;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBatchTest
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // 실제 Postgres 사용
class SampleJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanUp() {
        // 이전 실행(테스트 중 누적된 실행 데이터)을 정리하고 싶으면 주석 해제
        // jobRepositoryTestUtils.removeJobExecutions();
    }

    @Test
    void 메타테이블_INSERT_확인() throws Exception {
        // 실행 전 카운트
        long instBefore = count("select count(*) from batch_job_instance");
        long execBefore = count("select count(*) from batch_job_execution");

        // 항상 새로운 JobInstance가 되도록 unique 파라미터 추가
        JobParameters params = new JobParametersBuilder()
                .addLong("ts", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        // 상태 확인
        assertThat(execution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        // 실행 후 카운트
        long instAfter = count("select count(*) from batch_job_instance");
        long execAfter = count("select count(*) from batch_job_execution");

        // 최소한 instance 1건, execution 1건 이상 증가했는지 확인
        assertThat(instAfter).isEqualTo(instBefore + 1);
        assertThat(execAfter).isEqualTo(execBefore + 1);
    }

    private long count(String sql) {
        return jdbcTemplate.queryForObject(sql, Long.class);
    }
}
*/
