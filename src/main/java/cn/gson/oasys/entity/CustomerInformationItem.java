package cn.gson.oasys.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@Table(name = "customer_information_item")
public class CustomerInformationItem {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "customer_information_Id",columnDefinition = "关联id")
    private Long customerInformationId;
    @Column(name = "contact_person" , columnDefinition = "联系人")
    private String contactPerson;  // 联系人
    @Column(name = "department_job" , columnDefinition = "部门及职位")
    private String departmentJob;  // 部门及职务
    @Column(name = "contact_phone" , columnDefinition = "联系电话")
    private String contactPhone;   // 联系电话
    @Column(name = "update_person",columnDefinition = "更新用户")
    private Long updatePerson;
    @Column(name = "update_person_name",columnDefinition = "更新用户姓名")
    private String updatePersonName;
    @Column(name = "update_time",columnDefinition = "更新时间")
    private Date updateTime;
}
