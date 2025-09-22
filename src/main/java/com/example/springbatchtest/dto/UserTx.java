package com.example.springbatchtest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class UserTx {
    private String txId;
    private String userId;
    private String productCode;
    private BigDecimal amount;
    private LocalDate txDate;
}
