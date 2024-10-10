package cn.gson.oasys.entity.flowable;

import lombok.Data;

import java.sql.Timestamp;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ACT_RU_TASK")
@Data
public class ActRuTask {

    @Id
    @Column(name = "ID_", columnDefinition = "ID_ 示例: 15011")
    private String id;
    @Column(name = "REV_", columnDefinition = "REV_ 示例: 1")
    private Integer rev;
    @Column(name = "EXECUTION_ID_", columnDefinition = "EXECUTION_ID_ 示例: 15007")
    private String executionId;
    @Column(name = "PROC_INST_ID_", columnDefinition = "PROC_INST_ID_ 示例: 15001")
    private String procInstId;
    @Column(name = "PROC_DEF_ID_", columnDefinition = "PROC_DEF_ID_ 示例: project_process:4:12504")
    private String procDefId;
    @Column(name = "TASK_DEF_ID_", columnDefinition = "TASK_DEF_ID_ 示例: null (空)")
    private String taskDefId;
    @Column(name = "SCOPE_ID_", columnDefinition = "SCOPE_ID_ 示例: null (空)")
    private String scopeId;
    @Column(name = "SUB_SCOPE_ID_", columnDefinition = "SUB_SCOPE_ID_ 示例: null (空)")
    private String subScopeId;
    @Column(name = "SCOPE_TYPE_", columnDefinition = "SCOPE_TYPE_ 示例: null (空)")
    private String scopeType;
    @Column(name = "SCOPE_DEFINITION_ID_", columnDefinition = "SCOPE_DEFINITION_ID_ 示例: null (空)")
    private String scopeDefinitionId;
    @Column(name = "NAME_", columnDefinition = "NAME_ 示例: 业务拓展")
    private String name;
    @Column(name = "PARENT_TASK_ID_", columnDefinition = "PARENT_TASK_ID_ 示例: null (空)")
    private String parentTaskId;
    @Column(name = "DESCRIPTION_", columnDefinition = "DESCRIPTION_ 示例: null (空)")
    private String description;
    @Column(name = "TASK_DEF_KEY_", columnDefinition = "TASK_DEF_KEY_ 示例: project001")
    private String taskDefKey;
    @Column(name = "OWNER_", columnDefinition = "OWNER_ 示例: null (空)")
    private String owner;
    @Column(name = "ASSIGNEE_", columnDefinition = "ASSIGNEE_ 示例: $INITIATOR")
    private String assignee;
    @Column(name = "DELEGATION_", columnDefinition = "DELEGATION_ 示例: null (空)")
    private String delegation;
    @Column(name = "PRIORITY_", columnDefinition = "PRIORITY_ 示例: 50")
    private Integer priority;
    @Column(name = "CREATE_TIME_", columnDefinition = "CREATE_TIME_ 示例: 2024-09-23 15:17:28.654")
    private Timestamp createTime;
    @Column(name = "DUE_DATE_", columnDefinition = "DUE_DATE_ 示例: null (空)")
    private Date dueDate;
    @Column(name = "CATEGORY_", columnDefinition = "CATEGORY_ 示例: null (空)")
    private String category;
    @Column(name = "SUSPENSION_STATE_", columnDefinition = "SUSPENSION_STATE_ 示例: 1")
    private Integer suspensionState;
    @Column(name = "TENANT_ID_", columnDefinition = "TENANT_ID_ 示例: ")
    private String tenantId;
    @Column(name = "FORM_KEY_", columnDefinition = "FORM_KEY_ 示例: null (空)")
    private String formKey;
    @Column(name = "CLAIM_TIME_", columnDefinition = "CLAIM_TIME_ 示例: null (空)")
    private Date claimTime;
    @Column(name = "IS_COUNT_ENABLED_", columnDefinition = "IS_COUNT_ENABLED_ 示例: 1")
    private Boolean isCountEnabled;
    @Column(name = "VAR_COUNT_", columnDefinition = "VAR_COUNT_ 示例: 0")
    private Integer varCount;
    @Column(name = "ID_LINK_COUNT_", columnDefinition = "ID_LINK_COUNT_ 示例: 0")
    private Integer idLinkCount;
    @Column(name = "SUB_TASK_COUNT_", columnDefinition = "SUB_TASK_COUNT_ 示例: 0")
    private Integer subTaskCount;
}
