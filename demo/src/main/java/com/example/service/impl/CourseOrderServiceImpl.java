package com.example.service.impl;

import com.example.entity.Course;
import com.example.entity.CourseOrder;
import com.example.entity.PaymentRecord;
import com.example.entity.Student;
import com.example.entity.StudentCourseEnrollment;
import com.example.exception.BusinessException;
import com.example.mapper.CourseOrderMapper;
import com.example.mapper.PaymentRecordMapper;
import com.example.mapper.StudentCourseEnrollmentMapper;
import com.example.mapper.StudentMapper;
import com.example.service.CourseOrderService;
import com.example.service.CourseService;
import com.example.util.GradeMatchUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CourseOrderServiceImpl implements CourseOrderService {
    @Autowired
    private CourseOrderMapper courseOrderMapper;
    @Autowired
    private CourseService courseService;
    @Autowired
    private PaymentRecordMapper paymentRecordMapper;
    @Autowired
    private StudentCourseEnrollmentMapper enrollmentMapper;
    @Autowired
    private StudentMapper studentMapper;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override public List<CourseOrder> findAll() { return courseOrderMapper.findAll(); }
    @Override public CourseOrder findById(Integer id) { return courseOrderMapper.findById(id); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insert(CourseOrder entity) {
        int rows = courseOrderMapper.insert(entity);
        if (rows > 0 && isPending(entity.getStatus())) {
            createPendingPayment(entity);
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String applyPaid(Integer orderId) {
        CourseOrder order = courseOrderMapper.findById(orderId);
        if (order == null) {
            return "订单不存在";
        }
        if (order.getStatus() != null && order.getStatus() == 1) {
            return "订单已支付";
        }
        if (!isPending(order.getStatus())) {
            return "订单状态不可支付";
        }
        PaymentRecord payment = paymentRecordMapper.findByOrderId(orderId);
        if (payment == null) {
            createPendingPayment(order);
            payment = paymentRecordMapper.findByOrderId(orderId);
        }
        if (payment != null && payment.getStatus() != null && payment.getStatus() != 0) {
            return "支付单状态不可支付";
        }

        Course course = courseService.findById(order.getCourseId());
        String err = courseService.validatePurchasable(course);
        if (err != null) {
            return err;
        }

        Integer studentId = resolveStudentId(order, course);
        if (studentId == null) {
            return "未找到可选课的学生，请确认子女年级与课程匹配";
        }
        if (enrollmentMapper.findActiveByStudentAndCourse(studentId, order.getCourseId()) != null) {
            return "该学生已选过此课程";
        }

        int inc = courseService.incrementEnrolledCount(order.getCourseId());
        if (inc <= 0) {
            return "课程名额已满";
        }

        String now = nowText();
        StudentCourseEnrollment enrollment = new StudentCourseEnrollment();
        enrollment.setStudentId(studentId);
        enrollment.setCourseId(order.getCourseId());
        enrollment.setOrderId(orderId);
        enrollment.setParentId(order.getParentId());
        enrollment.setStatus(1);
        enrollment.setEnrollTime(now);
        enrollmentMapper.insert(enrollment);

        order.setStudentId(studentId);
        order.setStatus(1);
        order.setPayTime(now);
        courseOrderMapper.update(order);

        paymentRecordMapper.updateStatus(orderId, 1, now, null);
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(CourseOrder entity) {
        if (entity.getId() == null) {
            return 0;
        }
        CourseOrder old = courseOrderMapper.findById(entity.getId());
        if (old == null) {
            return 0;
        }
        int oldStatus = old.getStatus() != null ? old.getStatus() : 0;
        int newStatus = entity.getStatus() != null ? entity.getStatus() : oldStatus;

        if (oldStatus == 0 && newStatus == 1) {
            String err = applyPaid(entity.getId());
            if (err != null) {
                throw new BusinessException(err);
            }
            CourseOrder paid = courseOrderMapper.findById(entity.getId());
            entity.setPayTime(paid.getPayTime());
            entity.setStatus(1);
            entity.setStudentId(paid.getStudentId());
            return courseOrderMapper.update(entity);
        }

        if (oldStatus == 0 && newStatus == 2) {
            cancelPendingPayment(entity.getId());
            entity.setPayTime(null);
            return courseOrderMapper.update(entity);
        }

        if (oldStatus == 1 && newStatus != 1) {
            refundPaidOrder(old);
            entity.setPayTime(old.getPayTime());
            return courseOrderMapper.update(entity);
        }

        if (newStatus == 1 && (entity.getPayTime() == null || entity.getPayTime().isEmpty())) {
            entity.setPayTime(nowText());
        }
        return courseOrderMapper.update(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Integer id) {
        CourseOrder old = courseOrderMapper.findById(id);
        if (old == null) {
            return 0;
        }
        if (Objects.equals(old.getStatus(), 1)) {
            refundPaidOrder(old);
        } else if (isPending(old.getStatus())) {
            cancelPendingPayment(id);
        }
        return courseOrderMapper.deleteById(id);
    }

    @Override
    public boolean hasActiveEnrollment(Integer parentId, Integer courseId) {
        if (parentId == null || courseId == null) {
            return false;
        }
        return enrollmentMapper.countActiveByParentAndCourse(parentId, courseId) > 0;
    }

    private void createPendingPayment(CourseOrder order) {
        if (order.getId() == null) {
            return;
        }
        PaymentRecord existing = paymentRecordMapper.findByOrderId(order.getId());
        if (existing != null) {
            return;
        }
        PaymentRecord payment = new PaymentRecord();
        payment.setOrderId(order.getId());
        payment.setPaymentNo("PAY" + order.getOrderNo());
        payment.setAmount(order.getFee());
        payment.setStatus(0);
        payment.setPayChannel("simulated");
        paymentRecordMapper.insert(payment);
    }

    private void cancelPendingPayment(Integer orderId) {
        PaymentRecord payment = paymentRecordMapper.findByOrderId(orderId);
        if (payment != null && payment.getStatus() != null && payment.getStatus() == 0) {
            paymentRecordMapper.updateStatus(orderId, 2, null, nowText());
        }
    }

    private void refundPaidOrder(CourseOrder order) {
        enrollmentMapper.deactivateByOrderId(order.getId());
        courseService.decrementEnrolledCount(order.getCourseId());
        PaymentRecord payment = paymentRecordMapper.findByOrderId(order.getId());
        if (payment != null && payment.getStatus() != null && payment.getStatus() == 1) {
            paymentRecordMapper.updateStatus(order.getId(), 3, payment.getPayTime(), nowText());
        }
    }

    private Integer resolveStudentId(CourseOrder order, Course course) {
        if (order.getStudentId() != null) {
            return order.getStudentId();
        }
        List<Student> children = studentMapper.findAll().stream()
                .filter(s -> order.getParentId() != null && order.getParentId().equals(s.getParentId()))
                .filter(s -> GradeMatchUtil.isGradeMatch(course.getTargetGrade(), s.getClassName()))
                .collect(Collectors.toList());
        if (children.isEmpty()) {
            return null;
        }
        return children.get(0).getId();
    }

    private boolean isPending(Integer status) {
        return status == null || status == 0;
    }

    private String nowText() {
        return LocalDateTime.now().format(TIME_FMT);
    }
}
