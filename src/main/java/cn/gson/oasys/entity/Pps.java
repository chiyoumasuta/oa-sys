package cn.gson.oasys.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * 项目进度统计
 */

@Entity
@Table(name = "pps")
@Data
public class Pps {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "project_name", columnDefinition = "项目名称")
    private String projectName;
    @Column(name = "total_amount", columnDefinition = "预计金额")
    private Long totalAmount;
    @Column(name = "createTime", columnDefinition = "创建时间")
    private Date createTime;
    @Column(name = "create_user", columnDefinition = "创建人")
    private Long createUser;
    @Column(name = "create_user_name", columnDefinition = "创建人名称")
    private String createUserName;
    @Column(name = "dept_id", columnDefinition = "归属单位Id")
    private Long deptId;
    @Column(name = "dept_name", columnDefinition = "归属单位名称")
    private String deptName;
    @Column(name = "contact", columnDefinition = "联系人")
    private String contact;
    @Column(name = "contactnumber", columnDefinition = "联系电话")
    private String contactnumber;
    @Column(name = "head_id", columnDefinition = "项目追踪负责人")
    private Long headId;
    @Column(name = "head_name", columnDefinition = "项目追踪负责人姓名")
    private String headName;
    @Column(name = "invalid", columnDefinition = "是否作废")
    private Boolean invalid;
    @Transient
    private List<PpsItem> ppsItems;
}
