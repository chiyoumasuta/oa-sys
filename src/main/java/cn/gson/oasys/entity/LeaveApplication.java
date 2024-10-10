package cn.gson.oasys.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "leave_application")
@Data
public class LeaveApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "initiator", columnDefinition = "VARCHAR(255) COMMENT '发起人'")
    private String initiator;
    @Column(name = "leave_type", columnDefinition = "VARCHAR(255) COMMENT '请假类型'")
    private String leaveType;
    @Column(name = "start_time", columnDefinition = "TIMESTAMP COMMENT '开始时间'")
    private Date startTime;
    @Column(name = "start_period", columnDefinition = "VARCHAR(255) COMMENT '开始时间上午/下午'")
    private String startPeriod;
    @Column(name = "end_time", columnDefinition = "TIMESTAMP COMMENT '结束时间'")
    private Date endTime;
    @Column(name = "end_period", columnDefinition = "VARCHAR(255) COMMENT '结束时间上午/下午'")
    private String endPeriod;
    @Column(name = "duration", columnDefinition = "DECIMAL(5, 2) COMMENT '请假时长'")
    private Double duration;
    @Column(name = "reason", columnDefinition = "TEXT COMMENT '请假事由'")
    private String reason;
    @Column(name = "attachment_id", columnDefinition = "VARCHAR(255) COMMENT '附件ID'")
    private String attachmentId;
    @Column(name = "department", columnDefinition = "VARCHAR(255) COMMENT '所在部门'")
    private String department;
    @Column(name = "approver", columnDefinition = "VARCHAR(255) COMMENT '审批人'")
    private String approver;
    @Column(name = "cc_person", columnDefinition = "VARCHAR(255) COMMENT '抄送人'")
    private String ccPerson;
    @Column(name = "created_at", columnDefinition = "TIMESTAMP COMMENT '发起时间'")
    private Date createdAt;
    @Column(name = "stats", columnDefinition = "审核状态")
    private String stats;
    @Column(name = "process_instance_id", columnDefinition = "流程实例化id")
    private String processInstanceId;
    @Transient
    protected String initiatorName;
    @Transient
    protected String approverName;
    @Transient
    protected String ccPersonName;
    @Transient
    protected String departmentName;

    public Double getDuration() {
        return calculateLeaveDays();
    }

    public void setDuration() {
        this.duration = calculateLeaveDays();
    }

    public double calculateLeaveDays() {
        // 将开始时间和结束时间转为毫秒
        long startMillis = startTime.getTime();
        long endMillis = endTime.getTime();
        // 计算时间差（以天为单位）
        double leaveDays = (endMillis - startMillis) / (1000.0 * 60 * 60 * 24);
        // 处理上午和下午的情况
        if (startPeriod.equals("下午")) {
            leaveDays -= 0.5; // 如果是下午开始，减去0.5天
        }
        if (endPeriod.equals("上午")) {
            leaveDays -= 0.5; // 如果是上午结束，减去0.5天
        }
        // 确保最低为0.5天
        return Math.max(leaveDays, 0.5);
    }

}