package cn.gson.oasys.entity;

import lombok.Data;

import javax.management.relation.Role;
import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "department")
@Data
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", columnDefinition = "部门名称")
    private String name;
//    @Column(name = "manager_id", columnDefinition = "主管id")
//    private Long managerId; // 主管ID
    @Column(name = "deprecated", columnDefinition = "是否弃用")
    private boolean deprecated;
    @Transient
    private String role;

}
