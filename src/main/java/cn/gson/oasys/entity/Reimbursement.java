package cn.gson.oasys.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "reimbursement")
@Data
public class Reimbursement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "create_info",columnDefinition = "创建人信息")
    private String createInfo;
    @Column(name = "type",columnDefinition = "报销类型")
    private String type;
    @Column(name = "manner",columnDefinition = "部门主管")//可跳过
    private String manner;
    @Column(name = "amount",columnDefinition = "报销金额")
    private Double amount;
    @Column(name = "description",columnDefinition = "备注")
    private String description;
}
