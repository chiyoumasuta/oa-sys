package cn.gson.oasys.entity;

import cn.gson.oasys.vo.SysMenuVo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang.StringUtils;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(generator = "JDBC")
    @Column
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
    private Boolean enable;
    @Column
    private String role;
    @Column
    private String fiberArea;//空串代表超级管理员 显示所有区县
    @Column
    private String token;//token
    @Column
    private Integer type = 1;//用户可登录类型 1平台 2APP 3全部
    @Column
    private Boolean del = false;//true为删除
    @Column
    private Date loginAt;//最后登录时间
    @Column
    private Integer company;//公司
    @Column
    private Boolean resetPwd;
    @Column
    private Long deptId;//部门id
    @Column
    private boolean readUpgradeRecord;//是否已读最新升级日志
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
    private List<String> areaList;
    @Transient
    private String version;//升级信息

    public User(Long id, String loginName, String userName, String password, String phone, Boolean enable, String role, String fiberArea) {
        this.id = id;
        this.loginName = loginName;
        this.userName = userName;
        this.password = password;
        this.phone = phone;
        this.enable = enable;
        this.role = role;
        this.fiberArea = fiberArea;
    }

    public User() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return userName;
    }

/*    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }*/

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFiberArea() {
        return fiberArea;
    }

    public void setFiberArea(String fiberArea) {
        this.fiberArea = fiberArea;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Boolean getDel() {
        return del;
    }

    public void setDel(Boolean del) {
        this.del = del;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getTypeName() {
        return this.type == 1 ? "平台权限" : this.type == 2 ? "APP权限" : "全部";
    }

    public Date getLoginAt() {
        return loginAt;
    }

    public void setLoginAt(Date loginAt) {
        this.loginAt = loginAt;
    }

    public Integer getCompany() {
        return company;
    }

    public void setCompany(Integer company) {
        this.company = company;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public Boolean isResetPwd() {
        return resetPwd;
    }

    public void setResetPwd(Boolean resetPwd) {
        this.resetPwd = resetPwd;
    }

    public List<String> getAreaList() {
        if (areaList == null) {
            initAreaList();
        }
        return areaList;
    }

    public void setAreaList(List<String> areaList) {
        this.areaList = areaList;
    }

    public void initAreaList() {
        this.areaList = StringUtils.isNotBlank(fiberArea) ? Arrays.asList(this.fiberArea.split(",")) : new ArrayList<>();
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public List<SysRole> getRoles() {
        if (roles == null) {
            return new ArrayList<>();
        }
        return roles;
    }

    public void setRoles(List<SysRole> roles) {
        this.roles = roles;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public List<Long> getRoleIds() {
        if (this.roleIds == null) {
            return null;
        }
        return Arrays.asList(this.roleIds.split(",")).stream().map(Long::valueOf).collect(Collectors.toList());
    }

    public void setRoleIds(String roleIds) {
        this.roleIds = roleIds;
    }

    public List<SysMenuVo> getMenus() {
        return menus;
    }

    public void setMenus(List<SysMenuVo> menus) {
        this.menus = menus;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    //是否是系统内置超级管理员
    public boolean isAdmin() {
        return this.roles != null && this.roles.stream().filter(it -> it.getId() != null && it.getId().equals(1L)).collect(Collectors.toList()).size() > 0;
    }

    /**
     * 校验当前用户(User对象)是否有指定区县的权限
     *
     * @param fiberArea 需要校验的区县 多个使用逗号隔开
     * @return boolean true权限通过 false不通过
     */
    public boolean authByAreas(String fiberArea) {
        if (null != fiberArea && !"".equals(getFiberArea())) {
            //不是所有管辖区权限 验证是否有参数中的区县
            List<String> newAreas = Arrays.asList(fiberArea.split(","));
            List<String> oldAreas = getAreaList();
            if (oldAreas == null) {
                return false;
            }
            //如果等于0则说明鉴权成功 可以继续操作
            return newAreas.stream().filter(it -> !oldAreas.contains(it)).collect(Collectors.toList()).size() == 0;
        }
        return true;
    }

    public boolean getReadUpgradeRecord() {
        return readUpgradeRecord;
    }

    public void setReadUpgradeRecord(boolean readUpgradeRecord) {
        this.readUpgradeRecord = readUpgradeRecord;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
