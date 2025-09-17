package com.example.springbatchtest.batch.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TxAggMapper {
    // statementId만 쓰므로 메서드가 꼭 필요하진 않지만, 관례상 둬도 됩니다.
}