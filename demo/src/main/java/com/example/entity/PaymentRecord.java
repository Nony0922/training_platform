package com.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRecord {
    private Integer id;
    private Integer orderId;
    private String paymentNo;
    private BigDecimal amount;
    /** 0待支付 1已支付 2已取消 3已退款 */
    private Integer status;
    private String payChannel;
    private String payTime;
    private String finishTime;
    private String createTime;
}
