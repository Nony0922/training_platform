package com.example.mapper;

import com.example.entity.PaymentRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PaymentRecordMapper {
    PaymentRecord findByOrderId(Integer orderId);
    int insert(PaymentRecord entity);
    int update(PaymentRecord entity);
    int updateStatus(@Param("orderId") Integer orderId,
                     @Param("status") Integer status,
                     @Param("payTime") String payTime,
                     @Param("finishTime") String finishTime);
}
