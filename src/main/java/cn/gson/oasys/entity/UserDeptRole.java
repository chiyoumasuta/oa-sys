package cn.gson.oasys.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * 员工——部门——角色对应表
 */
@Entity
@Table(name = "user_dept_role")
@Data
public class UserDeptRole {
    @Id
    @GeneratedValue(generator = "JDBC",strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "department_id")
    private Long departmentId;

    private String role;
}