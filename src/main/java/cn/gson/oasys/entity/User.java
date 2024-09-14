package cn.gson.oasys.entity;

import cn.gson.oasys.vo.SysMenuVo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author l
 */
//@Entity
@Table(name = "user")
@Data
public class User {
    @Id
    @GeneratedValue(generator = "JDBC")
    @Column(name = "id")
    private Long id;
    @Column
    private String loginName;
    @Column
    private String userName;
    @JsonIgnore
    @Column
    private String password;
    @Column
    private String phone;
    @Column
    private String role;
    @Column
    private String token;//token
    @Column
    private Date loginAt;//最后登录时间
    @Column
    private String deptId;//部门id
    @Column
    private String posts;//职位
    @Column
    private boolean admin;//是否为管理员
    @Column
    private boolean del;//是否删除
    @Transient
    private List<SysRole> roles;
    @Transient
    private String roleName;
    @Transient
    private String roleIds;
    @Transient
    private String deptName;
    @Transient
    private List<SysMenuVo> menus;
    @Transient
    private Set<String> permissions;
    @Transient
    private boolean isManager;
}
