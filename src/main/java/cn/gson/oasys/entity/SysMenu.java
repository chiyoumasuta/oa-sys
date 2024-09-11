package cn.gson.oasys.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 菜单权限表 sys_menu
 */
//@Entity
@Table(name = "sys_menu")
@Data
public class SysMenu {
    /**
     * 菜单ID
     */
    @Id
    @GeneratedValue(generator = "JDBC")
    private Long id;
    /**
     * 菜单名称
     */
    @Column
    private String name;
    @Column
    private  String subTitle;
    @Column
    private String ancestors;
    /**
     * 父菜单ID
     */
    @Column
    private Long parentId;
    /**
     * 显示顺序
     */
    @Column
    private Integer orderNum;
    /**
     * 路由地址
     */
    @Column
    private String path;
    /**
     * 类型（M目录 C菜单 F按钮）
     */
    @Column
    private String menuType;
    /**
     * 菜单状态（0正常 1停用）
     */
    @Column
    private Integer status;
    /**
     * 权限字符串
     */
    @Column
    private String perms;
    /**
     * 参数
     */
    @Column
    private String param;

    /**
     * 菜单图标
     */
    @Column
    private String icon;
    /**
     * 备注
     */
    @Column
    private String remark;
    /**
     * 父菜单名称
     */
    @Transient
    private String parentName;
    @Transient
    private Boolean disabled = false;//是否禁用
    @Transient
    private Boolean hide = false;//是否显示 默认是
    @Transient
    private String statusName;

    /**
     * 子菜单
     */
    @Transient
    private List<SysMenu> children = new ArrayList<SysMenu>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getAncestors() {
        return ancestors;
    }

    public void setAncestors(String ancestors) {
        this.ancestors = ancestors;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMenuType() {
        return menuType;
    }

    public void setMenuType(String menuType) {
        this.menuType = menuType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getPerms() {
        return perms;
    }

    public void setPerms(String perms) {
        this.perms = perms;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public Boolean getHide() {
        return hide;
    }

    public void setHide(Boolean hide) {
        this.hide = hide;
    }

    public List<SysMenu> getChildren() {
        if (children == null || children.size() == 0) {
            return null;
        }
        return children;
    }

    public void setChildren(List<SysMenu> children) {
        this.children = children;
    }

    public String getStatusName() {
        return SysMenu.Status.getNameByCode(this.status);
    }

    public enum Status {
        NORMAL(0, "正常"),
        STOP(1, "停用");

        private Integer code;
        private String name;

        Status(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        public static String getNameByCode(Integer code) {
            for (SysRole.Status value : SysRole.Status.values()) {
                if (value.getCode().equals(code)) {
                    return value.getName();
                }
            }
            return null;
        }

        public Integer getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Override
    public String toString() {
        return "SysMenu{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", ancestors='" + ancestors + '\'' +
                ", parentId=" + parentId +
                ", orderNum=" + orderNum +
                ", path='" + path + '\'' +
                ", menuType='" + menuType + '\'' +
                ", status=" + status +
                ", perms='" + perms + '\'' +
                ", icon='" + icon + '\'' +
                ", remark='" + remark + '\'' +
                ", parentName='" + parentName + '\'' +
                ", disabled=" + disabled +
                ", hide=" + hide +
                ", statusName='" + statusName + '\'' +
                ", children=" + children +
                '}';
    }
}
