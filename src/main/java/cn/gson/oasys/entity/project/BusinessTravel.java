package cn.gson.oasys.entity.project;

import lombok.Data;

import javax.persistence.*;

/**
 * 出差/外出访问
 */
@Entity
@Table(name = "business_travel")
@Data
public class BusinessTravel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "create_info",columnDefinition = "发出人信息")
    private String createInfo;
    @Column(name = "header",columnDefinition = "负责人")
    private String header;
    @Column(name = "travel_file",columnDefinition = "出差报告")
    private Long travelFile;
    @Column(name = "customer_analytics",columnDefinition = "客户分析")
    private String customerAnalytics;
    @Column(name = "problem",columnDefinition = "问题痛点")
    private String problem;
    @Column(name = "budget",columnDefinition = "费用预算")
    private String budget;
    @Column(name = "member",columnDefinition = "参与成员")
    private String member;
    @Column(name = "pass",columnDefinition = "是否通过")
    private boolean pass;
}