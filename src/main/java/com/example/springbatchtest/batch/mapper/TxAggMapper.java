package com.example.springbatchtest.batch.mapper;

import com.example.springbatchtest.dto.DayAgg;
import com.example.springbatchtest.dto.MonthAgg;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Mapper
public interface TxAggMapper {
    MonthAgg selectMonthAggPage(int from, int to, int _pagesize, int _skiprows);
}