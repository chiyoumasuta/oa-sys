package cn.gson.oasys.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name="operation")
public class Operation {

    //所操作的菜单
    public enum Menu{
//        FIBER_MANAGE("光缆管理"),
//        FIBER_CORE_MANAGE("纤芯管理"),
//        USER_MANAGE("人员管理"),
//        SYS_CONFIG("系统配置"),
//        ANALYSIS_STATISTICS("分析统计"),
        OTHER("其它");

        String name;

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        Menu(String name){
            this.name = name;
        }

    }

    //操作类型
    public enum Type{
        IMPORT_FILE("导入文件"),
        EXPORT_FILE("导出文件"),
        CHANGE_PWD("修改密码"),
        DEL("删除信息"),
        LOGIN("登录"),
        LOGOUT("登出");

        String name;

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        Type(String name){
            this.name = name;
        }
    }

    @Id
    private Long id;

    private String user;

    private Date time;

    private Operation.Menu menu;

    private Operation.Type type;

    //具体的操作描述 可选
    @Column(name="`desc`")
    private String desc;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUser() {
        return this.user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Date getTime() {
        return this.time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Operation.Menu getMenu() {
        return this.menu;
    }

    public void setMenu(Operation.Menu menu) {
        this.menu = menu;
    }

    public Operation.Type getType() {
        return this.type;
    }

    public void setType(Operation.Type type) {
        this.type = type;
    }

    public String getDesc() {
        return this.desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getTypeName(){
        return this.type == null ? "":this.type.getName();
    }

    public String getMenuName(){
        return this.menu == null ? "":this.menu.getName();
    }
}
