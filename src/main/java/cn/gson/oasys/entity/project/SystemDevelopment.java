package cn.gson.oasys.entity.project;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * 系统研发
 */
@Entity
@Table(name = "system_development")
@Data
public class SystemDevelopment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "header",columnDefinition = "项目经理")
    private String header;
    @Column(name = "content",columnDefinition = "实施内容")
    private String content;
    @Column(name = "plan",columnDefinition = "工期计划")
    private String plan;
    @Column(name = "budget",columnDefinition = "成本预算")
    private String budget;
    @Column(name = "logs",columnDefinition = "实施日志")
    private String logs;
    @Column(name = "checks",columnDefinition = "工期检查")
    private String checks;
    @Column(name = "last_check_time",columnDefinition = "最后一次工期检查时间")
    private Date lastCheckTime;
    @Column(name = "start_time",columnDefinition = "开始施工时间")
    private Date startTime;
    @Column(name = "end_time",columnDefinition = "施工结束时间")
    private Date endTime;
}
