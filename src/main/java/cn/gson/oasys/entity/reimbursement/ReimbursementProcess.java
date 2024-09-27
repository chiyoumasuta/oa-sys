package cn.gson.oasys.entity.reimbursement;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * 报销流程业务数据表
 */
@Entity
@Table(name = "reimbursement_process")
@Data
public class ReimbursementProcess {
    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "department", columnDefinition = "部门")    private String department;
    @Column(name = "expense_type", columnDefinition = "费用类型(差旅费/施工费，下拉选择)")    private ExpenseType type;
    @Column(name = "submit_date", columnDefinition = "提交日期")    private Date submitDate;
    @Column(name = "approval_date", columnDefinition = "批准日期")    private Date approvalDate;
    @Column(name = "file_list", columnDefinition = "文件列表(支持多个)")    private String fileList;
    @Column(name = "status", columnDefinition = "状态(提交/审核/批准/驳回，按照阶段自动显示)")    private Status status;
    @Column(name = "approver", columnDefinition = "审批人")    private String approver;
    @Column(name = "reimbursement_amount", columnDefinition = "报销金额")    private Double reimbursementAmount;
    @Column(name = "actual_amount", columnDefinition = "实际金额")    private Double actualAmount;
    @Column(name = "project", columnDefinition = "所属项目")    private String project;
    @Transient
    private String typeName;
    @Transient
    private String statusName;

    public enum Status{
        SUBMITTED("提交"),
        UNDER_REVIEW("审核"),
        APPROVED("批准"),
        REJECTED("驳回");
        private final String name;

        Status(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public enum ExpenseType {
        TRAVEL_EXPENSE("差旅费"),
        CONSTRUCTION_EXPENSE("施工费"),
        MATERIAL_EXPENSE("材料费"), // 示例扩展类型
        OTHER_EXPENSE("其他费用");    // 示例扩展类型

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
}

