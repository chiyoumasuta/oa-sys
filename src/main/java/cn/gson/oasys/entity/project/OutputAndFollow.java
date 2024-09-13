package cn.gson.oasys.entity.project;

import lombok.Data;

import javax.persistence.*;
import java.util.Map;

/**
 * 方案输出&方案跟进
 */
@Entity
@Table(name = "output_and_follow")
@Data
public class OutputAndFollow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "program_file",columnDefinition = "输出方案文件")
    private String programFile;
    @Column(name = "minute",columnDefinition = "评审会记录")
    private Integer minute;
    @Column(name = "due_minute",columnDefinition = "角色分工")
    private Integer dueMinute;
    @Column(name = "important_info",columnDefinition = "重要信息")
    private String importantInfo;
    @Column(name = "stats",columnDefinition = "方案状态")
    private String stats;
    @Column(name = "pass",columnDefinition = "方案是否通过")
    private String pass;
    @Column(name = "rejection_message",columnDefinition = "方案驳回记录")
    private String rejectionMessage;
    @Transient
    private Map<String,String> RejectInfo;
}
