package com.example.springbatchtest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class DayAgg {
    private LocalDate txDate;
    private BigDecimal totalAmount;
    private int txCount;

}
