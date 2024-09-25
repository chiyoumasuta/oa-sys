package cn.gson.oasys.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "leave")
@Data
public class Leave {
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
}