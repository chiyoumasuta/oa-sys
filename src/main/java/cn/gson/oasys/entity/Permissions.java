package cn.gson.oasys.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "permissions")
@Data
public class Permissions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "parent_id", columnDefinition = "父权限id")
    private Long parentId;
    @Column(name = "label", columnDefinition = "权限解释")
    private String label;
    @Column(name = "name", columnDefinition = "权限名")
    private String roleDesc;
    @Column(name = "icon", columnDefinition = "图标")
    private String icon;
    @Column(name = "type", columnDefinition = "类型")
    private String type;
    @Column(name = "route",columnDefinition = "权限路径")
    private String route;
    @Column(name = "component", columnDefinition = "引用资源路径")
    private String component;
    @Column(name = "is_delete", columnDefinition = "是否删除")
    private boolean isDelete;
    @Transient
    private List<Permissions> children;
}
