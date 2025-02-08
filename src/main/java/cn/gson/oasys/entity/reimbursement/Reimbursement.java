package cn.gson.oasys.entity.reimbursement;

import cn.gson.oasys.entity.File;
import cn.gson.oasys.entity.ProjectProcess;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 报销流程业务数据表
 */
@Entity
@Table(name = "reimbursement")
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Reimbursement {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "department", columnDefinition = "部门")
    private String department;
    @Column(name = "department_name", columnDefinition = "部门名称")
    private String departmentName;
    @Column(name = "expense_type", columnDefinition = "费用类型")
    private ExpenseType type;
    @Column(name = "submit_user", columnDefinition = "提交人")
    private Long submitUser;
    @Column(name = "submit_user_name", columnDefinition = "提交人名称")
    private String submitUserName;
    @Column(name = "submit_date", columnDefinition = "提交日期")
    private Date submitDate;
    @Column(name = "approval_date", columnDefinition = "批准日期")
    private Date approvalDate;
    @Column(name = "attachment_id", columnDefinition = "文件列表(支持多个)")
    private String attachmentId;
    @Column(name = "status", columnDefinition = "状态(提交/审核/批准/驳回，按照阶段自动显示)")
    private Status status;
    @Column(name = "approver", columnDefinition = "审批人")
    private Long approver;
    @Column(name = "approver_name", columnDefinition = "审批人")
    private String approverName;
    @Column(name = "approver_time", columnDefinition = "审批时间")
    private Date approverTime;
    @Column(name = "accounting_time", columnDefinition = "财务审核时间")
    private Date accountingTime;
    @Column(name = "reimbursement_amount", columnDefinition = "报销金额")
    private Double reimbursementAmount;
    @Column(name = "actual_amount", columnDefinition = "实际金额")
    private Double actualAmount;
    @Column(name = "project", columnDefinition = "所属项目")
    private String project;
    @Column(name = "start_time", columnDefinition = "开始时间")
    private Date startTime;
    @Column(name = "start_period", columnDefinition = "开始时间上午/下午")
    private String startPeriod;
    @Column(name = "end_time", columnDefinition = "结束时间")
    private Date endTime;
    @Column(name = "end_period", columnDefinition = "结束时间上午/下午")
    private String endPeriod;
    @Column(name = "duration", columnDefinition = "时长")
    private Double duration;
    @Column(name = "place", columnDefinition = "地点")
    private String place;
    @Column(name = "company", columnDefinition = "拜访单位")
    private String company;
    @Column(name = "business_travel", columnDefinition = "关联工单(出差申请)")
    private String businessTravel;
    @Column(name = "opinions", columnDefinition = "审核意见")
    private String opinions;
    @Column(name = "deploy_id",columnDefinition = "流程id")
    private String deployId;
    @Column(name = "deploy_type",columnDefinition = "流程类型")
    private String deployType;
    @Column(name = "no",columnDefinition = "唯一编号")
    private String no;
    @Transient
    private List<String> opinionsList;
    @Transient
    private String typeName;
    @Transient
    private String statusName;
    @Transient
    private List<ReimbursementItem> details;
    @Transient
    private List<File> fileList;

    public enum Status {
        REVIEW_1("宋云潇审核",1),//宋云潇
        REVIEW_2("马涛审核",2),//马涛
        ACCOUNTING("财务审核", 3),
        GENERAL("总经理审核", 4),
        APPROVED("批准", 5),
        REJECTED("驳回", 6),
        REJECTED_SELF("退回",7);
        private final String name;
        private final int leave;

        Status(String name, int leave) {
            this.name = name;
            this.leave = leave;
        }

        public String getName() {
            return name;
        }

        public int getLeave() {
            return leave;
        }

        public static Status getNextStatus(Status status) {
            for (Status value : Status.values()) {
                if (value.getLeave() == (status.getLeave() + 1)) {
                    return value;
                }
            }
            return null;
        }
    }

    public enum ExpenseType {
        TRAVEL_EXPENSES("差旅费"),
        IMPLEMENTATION_FEE("实施费"),
        DAILY_EXPENSES("日常开支"),
        AMORTIZATION("摊销费用");

        private final String name;

        ExpenseType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public String getTypeName() {
        return this.type.getName();
    }

    public void setTypeName() {
        this.typeName = this.type.getName();
    }

    public String getStatusName() {
        return this.status.getName();
    }

    public void setStatusName() {
        this.statusName = this.status.getName();
    }

    public Double getDuration() {
        return calculateLeaveDays();
    }

    public void setDuration() {
        this.duration = calculateLeaveDays();
    }

    public double calculateLeaveDays() {
        // 将 Date 转换为 LocalDate
        LocalDate startDate = startTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = endTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        // 计算日期差（忽略时间部分）
        long dateDiff = ChronoUnit.DAYS.between(startDate, endDate);

        // 初始假期天数（如果跨越两天，至少算1天）
        double leaveDays = dateDiff + 1; // 默认情况算作 1 天或更多

        // 处理开始时间和结束时间的上午下午情况
        if (startPeriod.equals("下午")) {
            leaveDays -= 0.5; // 如果是下午开始，减去0.5天
        }
        if (endPeriod.equals("上午")) {
            leaveDays -= 0.5; // 如果是上午结束，减去0.5天
        }

        // 确保最小假期为 0.5 天
        return Math.max(leaveDays, 0.5);
    }

    public List<String> getOpinionsList() {
        return this.opinions==null?null:Arrays.asList(this.opinions.split(";"));
    }

    public void setOpinionsList(List<String> opinionsList) {
        this.opinionsList = this.opinions==null?null:Arrays.asList(this.opinions.split(";"));
    }
}

