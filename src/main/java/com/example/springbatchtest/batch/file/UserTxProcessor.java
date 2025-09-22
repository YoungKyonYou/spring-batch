package com.example.springbatchtest.batch.file;


import com.example.springbatchtest.dto.UserTx;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserTxProcessor {

    @Bean
    public ItemProcessor<UserTx, UserTx> userTxProcessorBean() {
        return in -> {
            if (in.getAmount() == null || in.getAmount().signum() < 0) {
                return null;
            }
            return in;
        };
    }
}