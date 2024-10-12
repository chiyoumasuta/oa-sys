package cn.gson.oasys.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * 项目追踪明细表
 */

@Entity
@Table(name = "pps_item")
@Data
public class PpsItem {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "pps_id", columnDefinition = "主表id")
    private Long ppsId;
    @Column(name = "create_user", columnDefinition = "更新人")
    private Long createUser;
    @Column(name = "create_user_name", columnDefinition = "更新人名称")
    private String createUserName;
    @Column(name = "create_time", columnDefinition = "更新时间")
    private Date createTime;
    @Column(name = "info", columnDefinition = "进度描述")
    private String info;
}
