package cn.gson.oasys.entity;


import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Data
@Table(name = "customer_information")
public class CustomerInformation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "architecture",columnDefinition = "体系")
    private String architecture;         // 体系
    @Column(name = "company" , columnDefinition = "公司")
    private String company;        // 公司
    @Column(name = "company_address" , columnDefinition = "公司详情地址")
    private String companyAddress; // 公司详细地址
    @Column(name = "is_delete",columnDefinition = "是否删除")
    private boolean isDelete;
    @Transient
    private List<CustomerInformationItem> items;
}
