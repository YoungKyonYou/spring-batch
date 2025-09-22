package com.example.springbatchtest.batch.file.importfile;


import com.example.springbatchtest.batch.mapper.TxAggMapper;
import com.example.springbatchtest.dto.ProductDim;
import com.example.springbatchtest.dto.UserDim;
import com.example.springbatchtest.dto.UserTx;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserTxEnrichingMyBatisWriter implements ItemWriter<UserTx> {
    private final SqlSessionTemplate batchSqlSessionTemplate; // ExecutorType.BATCH
    private final TxAggMapper dimMapper;


    @Override
    public void write(List<? extends UserTx> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        for (UserTx it : items) {
            if (it.getAmount() != null) {
                //BigDecimal 값을 소수점 둘째 자리까지 반올림해서 맞추는 처리
                it.setAmount(it.getAmount().setScale(2, RoundingMode.HALF_UP));
            }
        }

        List<String> userIds = items.stream().map(UserTx::getUserId).filter(Objects::nonNull).distinct().collect(
                Collectors.toList());
        List<String> productCodes = items.stream().map(UserTx::getProductCode).filter(Objects::nonNull).distinct()
                .collect(
                        Collectors.toList());

        Map<String, UserDim> userMap = dimMapper.selectByUserIds(userIds)
                .stream().collect(Collectors.toMap(UserDim::getUserId, u -> u));

        Map<String, ProductDim> productMap = dimMapper.selectByProductCodes(productCodes)
                .stream().collect(Collectors.toMap(ProductDim::getProductCode, p -> p));

        for (String uid : userIds) {
            if (!userMap.containsKey(uid)) {
                dimMapper.insertUserIgnore(new UserDim() {{
                    setUserId(uid);
                }});
            }
        }
        for (String code : productCodes) {
            if (!productMap.containsKey(code)) {
                dimMapper.insertProductIgnore(new ProductDim() {{
                    setProductCode(code);
                }});
            }
        }

        for (UserTx it : items) {
            if (it.getAmount() == null || it.getAmount().signum() < 0) {
                continue;
            }
            dimMapper.upsert(it);
        }

        //배치 flush (메모리 누수 방지 및 즉시 Statement 실행)
        batchSqlSessionTemplate.flushStatements();
    }
}
