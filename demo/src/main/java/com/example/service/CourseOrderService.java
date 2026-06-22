package com.example.service;

import com.example.entity.CourseOrder;
import java.util.List;

public interface CourseOrderService {
    List<CourseOrder> findAll();
    CourseOrder findById(Integer id);
    int insert(CourseOrder entity);
    int update(CourseOrder entity);
    int deleteById(Integer id);
    /** 支付成功：更新订单、写入选课记录、更新名额，并同步支付流水 */
    String applyPaid(Integer orderId);

    /** 家长是否已有该课程的有效选课 */
    boolean hasActiveEnrollment(Integer parentId, Integer courseId);
}
