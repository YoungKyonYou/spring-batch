package com.example.springbatchtest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;

@Getter
public class DayAgg {
    private final LocalDate txDate;
    private final BigDecimal totalAmount;
    private final int txCount;

    public DayAgg(LocalDate txDate, BigDecimal totalAmount, int txCount) {
        this.txDate = txDate;
        this.totalAmount = totalAmount;
        this.txCount = txCount;
    }

}
