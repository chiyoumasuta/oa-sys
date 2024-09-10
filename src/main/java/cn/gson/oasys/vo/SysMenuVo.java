package cn.gson.oasys.vo;

import java.util.List;

public class SysMenuVo {
    private Long id;
    private String name;//名称
    private String subTitle;//子名称
    private String icon;//图标
    private String path;//路径
    private String perms;//权限字符串
    private String param;//参数
    private Boolean hide; //是否隐藏
    private Boolean disabled;//是否禁用

    private List<SysMenuVo> children;//子菜单

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

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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

    public Boolean getHide() {
        return hide;
    }

    public void setHide(Boolean hide) {
        this.hide = hide;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public List<SysMenuVo> getChildren() {
        if(children == null || children.size() == 0) {
            return null;
        }
        return children;
    }

    public void setChildren(List<SysMenuVo> children) {
        this.children = children;
    }
}
