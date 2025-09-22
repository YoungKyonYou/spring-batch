package com.example.springbatchtest.batch.file;

import com.example.springbatchtest.dto.UserTx;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

@Configuration
public class UserTxWhitespaceReader {

    @Bean
    @StepScope
    public FlatFileItemReader<UserTx> userTxReader(
            @Value("#{jobParameters['inputFile']}") String inputFile) {

        return new FlatFileItemReaderBuilder<UserTx>()
                .name("userTxReader")
                .resource(new FileSystemResource(inputFile))
                .encoding("UTF-8")
                .strict(true)     // 파일 없으면 실패
                .saveState(true)  // ExecutionContext에 reader 상태 저장(메타테이블)
                .linesToSkip(0)   // 헤더 없음(파일 처음에서 몇 줄을 무시할지 지정) 여기선 0개를 무시 즉 첫줄 부터 읽음
                .lineMapper((line, lineNumber) -> {
                    String[] a = line.trim().split("\\s+");
                    if (a.length < 5) {
                        throw new IllegalArgumentException("잘못된 컬럼 수(line " + lineNumber + "): " + line);
                    }
                    UserTx u = new UserTx();
                    u.setTxId(a[0]);
                    u.setUserId(a[1]);
                    u.setProductCode(a[2]);

                    try {
                        u.setAmount(new BigDecimal(a[3]));
                    } catch (Exception e) {
                        throw new IllegalArgumentException("amount 파싱 실패(line " + lineNumber + "): " + a[3], e);
                    }

                    try {
                        u.setTxDate(LocalDate.parse(a[4])); // yyyy-MM-dd
                    } catch (Exception e) {
                        throw new IllegalArgumentException("txDate 파싱 실패(line " + lineNumber + "): " + a[4], e);
                    }

                    return u;
                })
                .build();
    }
}