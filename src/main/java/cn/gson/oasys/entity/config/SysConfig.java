package cn.gson.oasys.entity.config;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "sys_config")
@Data
public class SysConfig {
    @Id
    @Column(name = "id")
    private int id;
    @Column(name = "name", columnDefinition = "配置名称")
    private String name;
    @Column(name = "value", columnDefinition = "配置信息")
    private String value;
}
