package cn.gson.oasys.entity.reimbursement;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "reimbursement_list")
@Data
public class ReimbursementItem {
    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "reimbursement_id",columnDefinition = "主表")
    private Long reimbursementId;
    @Column(name = "index_id",columnDefinition = "序号")
    private Integer index;
    @Column(name = "type",columnDefinition = "项目名称(出差,日常)")
    private String type;
    @Column(name = "cost",columnDefinition = "费用占比(出差,日常)")
    private String cost;
    @Column(name = "participants",columnDefinition = "参与人(施工)")
    private String participants;
    @Column(name = "days",columnDefinition = "参与天数(施工)")
    private String days;
    @Column(name = "remark",columnDefinition = "备注")
    private String remark;
}
