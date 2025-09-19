package com.example.springbatchtest.service;

import com.example.springbatchtest.dto.DayAgg;
import com.example.springbatchtest.dto.MonthAgg;

public interface MonthService {
    MonthAgg selectMonthAgg(DayAgg dayAgg);
}
