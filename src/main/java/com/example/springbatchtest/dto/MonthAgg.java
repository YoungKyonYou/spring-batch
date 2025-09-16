package com.example.springbatchtest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;

@Getter
public class MonthAgg {
    private final LocalDate ymDate;
    private final BigDecimal totalAmount;
    private final int txCount;

    public MonthAgg(LocalDate ymDate, BigDecimal totalAmount, int txCount) {
        this.ymDate = ymDate;
        this.totalAmount = totalAmount;
        this.txCount = txCount;
    }
}
