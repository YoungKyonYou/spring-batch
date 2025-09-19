package com.example.springbatchtest.service.impl;

import com.example.springbatchtest.batch.mapper.TxAggMapper;
import com.example.springbatchtest.dto.DayAgg;
import com.example.springbatchtest.dto.MonthAgg;
import com.example.springbatchtest.service.MonthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MonthServiceImpl implements MonthService {
    private final TxAggMapper txAggMapper;

    @Override
    public MonthAgg selectMonthAgg(DayAgg dayAgg) {
        return txAggMapper.selectMonthAggPage(0, 1, 0, 0);
    }
}
