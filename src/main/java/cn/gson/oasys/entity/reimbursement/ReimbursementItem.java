package cn.gson.oasys.entity.reimbursement;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.stereotype.Repository;

import javax.persistence.*;

@Entity
@Table(name = "reimbursement_item")
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReimbursementItem {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "reimbursement_id", columnDefinition = "主表")
    private Long reimbursementId;
    @Column(name = "index_id", columnDefinition = "序号")
    private Integer index;
    @Column(name = "type", columnDefinition = "报销项目名称(出差,日常)")
    private String type;
    @Column(name = "cost", columnDefinition = "报销费用(出差,日常)")
    private Double cost;
    @Column(name = "project", columnDefinition = "所属项目")
    private String project;
    @Column(name = "participants", columnDefinition = "参与人(施工,出差)id")
    private String participants;
    @Column(name = "participants_name", columnDefinition = "参与人(施工,出差)用户名")
    private String participantsName;
    @Column(name = "dept",columnDefinition = "部门")
    private String dept;
    @Column(name = "days", columnDefinition = "参与天数(施工)")
    private Double days;
    @Column(name = "remark", columnDefinition = "备注")
    private String remark;
}
