package cn.gson.oasys.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "role")
@Data
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "role_name", columnDefinition = "角色名称")
    private String roleName;
    @Column(name = "permissions", columnDefinition = "权限名")
    private String permissions;
    @Transient
    private List<Permissions> permissionList;
}
