package cn.gson.oasys.entity;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * 角色表 sys_role
 */
@Entity
@Table(name = "sys_role")
public class SysRole {

    /**
     * 角色ID
     */
    @Id
    @GeneratedValue(generator = "JDBC")
    private Long id;

    /**
     * 角色名称
     */
    @Column
    private String roleName;

    /**
     * 角色权限
     */
    @Column
    private String roleKey;


    /**
     * 角色状态（0正常 1停用）
     */
    @Column
    private Integer status;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    @Column
    private Integer delFlag;
    @Column
    private String createBy;
    @Column
    private Date createTime;
    @Column
    private String updateBy;
    @Column
    private Date updateTime;
    /**
     * 备注
     */
    @Column
    private String remark;

    @Column
    private boolean isDefault;//是否内置角色

    /**
     * 用户是否存在此角色标识 默认不存在
     */
    @Transient
    private boolean flag = false;

    /**
     * 菜单组
     */
    @Transient
    private List<String> menuIds;

    /**
     * 部门组（数据权限）
     */
    @Transient
    private List<String> deptIds;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleKey() {
        return roleKey;
    }

    public void setRoleKey(String roleKey) {
        this.roleKey = roleKey;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(Integer delFlag) {
        this.delFlag = delFlag;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public List<String> getMenuIds() {
        return menuIds;
    }

    public void setMenuIds(List<String> menuIds) {
        this.menuIds = menuIds;
    }

    public List<String> getDeptIds() {
        return deptIds;
    }

    public void setDeptIds(List<String> deptIds) {
        this.deptIds = deptIds;
    }

    public String getStatusName() {
        return Status.getNameByCode(this.status);
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
            for (Status value : Status.values()) {
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
}
