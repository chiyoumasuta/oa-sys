package cn.gson.oasys.entity.reimbursement;

import cn.gson.oasys.entity.ProjectProcess;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * 报销流程业务数据表
 */
@Entity
@Table(name = "reimbursement")
@Data
public class Reimbursement {
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "department", columnDefinition = "部门")
    private Long department;
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
    @Transient
    private String typeName;
    @Transient
    private String statusName;
    @Transient
    private List<ReimbursementItem> details;

    public enum Status {
        MANAGER("部门负责人审核", 1),
        ACCOUNTING("财务审核", 2),
        GENERAL("总经理审核", 3),
        APPROVED("批准", 4),
        REJECTED("驳回", 5);
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
        DAILY_EXPENSES("日常开支");

        private final String name;

        ExpenseType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public String getTypeName() {
        return type.getName();
    }

    public void setTypeName(String typeName) {
        this.typeName = type.getName();
    }

    public String getStatusName() {
        return status.getName();
    }

    public void setStatusName(String statusName) {
        this.statusName = status.getName();
    }

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

