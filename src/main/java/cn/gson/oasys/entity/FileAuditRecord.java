package cn.gson.oasys.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "audit_record")
@Data
public class FileAuditRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "file_id", columnDefinition = "文件Id")
    private Long fileId;
    @Column(name = "file_name", columnDefinition = "文件名")
    private String fileName;
    @Column(name = "submit_user_id", columnDefinition = "提交用户Id")
    private Long submitUserId;
    @Column(name = "submit_user_name", columnDefinition = "提交用户名称")
    private String submitUserName;
    @Column(name = "submit_time", columnDefinition = "提交时间")
    private Date submitTime;
    @Column(name = "person_in_charge", columnDefinition = "处理人")
    private Long personInCharge;
    @Column(name = "person_in_charge_name", columnDefinition = "审核用户名称")
    private String personInChargeName;
    @Column(name = "result", columnDefinition = "审核结果")
    private String result;
    @Column(name = "audit_time", columnDefinition = "审核时间")
    private Date auditTime;
}
