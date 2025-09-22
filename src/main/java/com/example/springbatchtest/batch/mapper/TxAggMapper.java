package com.example.springbatchtest.batch.mapper;

import com.example.springbatchtest.dto.DayAgg;
import com.example.springbatchtest.dto.MonthAgg;
import com.example.springbatchtest.dto.ProductDim;
import com.example.springbatchtest.dto.UserDim;
import com.example.springbatchtest.dto.UserTx;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface TxAggMapper {


    List<DayAgg> selectDayAggPage(@Param("from") String from,
                                  @Param("to") String to,
                                  @Param("_pagesize") int pageSize,
                                  @Param("_skiprows") int skipRows);


    int upsertDailyAgg(DayAgg dayAgg);


    List<MonthAgg> selectMonthAggPage(@Param("from") String from,
                                      @Param("to") String to,
                                      @Param("_pagesize") int pageSize,
                                      @Param("_skiprows") int skipRows);

    int upsertMonthlyAgg(MonthAgg monthAgg);

    List<UserDim> selectByUserIds(@Param("userIds") List<String> userIds);

    int insertUserIgnore(UserDim user);

    List<ProductDim> selectByProductCodes(@Param("codes") List<String> codes);

    int insertProductIgnore(ProductDim product);

    int upsert(UserTx tx);
}