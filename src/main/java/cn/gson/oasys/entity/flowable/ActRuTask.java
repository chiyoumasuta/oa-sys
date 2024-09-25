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
    @Column(name = "ID_") private String id;                   // ID_ 示例: "15011"
    @Column(name = "REV_") private Integer rev;                 // REV_ 示例: 1
    @Column(name = "EXECUTION_ID_") private String executionId;          // EXECUTION_ID_ 示例: "15007"
    @Column(name = "PROC_INST_ID_") private String procInstId;           // PROC_INST_ID_ 示例: "15001"
    @Column(name = "PROC_DEF_ID_") private String procDefId;            // PROC_DEF_ID_ 示例: "project_process:4:12504"
    @Column(name = "TASK_DEF_ID_") private String taskDefId;            // TASK_DEF_ID_ 示例: null (空)
    @Column(name = "SCOPE_ID_") private String scopeId;              // SCOPE_ID_ 示例: null (空)
    @Column(name = "SUB_SCOPE_ID_") private String subScopeId;           // SUB_SCOPE_ID_ 示例: null (空)
    @Column(name = "SCOPE_TYPE_") private String scopeType;            // SCOPE_TYPE_ 示例: null (空)
    @Column(name = "SCOPE_DEFINITION_ID_") private String scopeDefinitionId;    // SCOPE_DEFINITION_ID_ 示例: null (空)
    @Column(name = "NAME_") private String name;                 // NAME_ 示例: "业务拓展"
    @Column(name = "PARENT_TASK_ID_") private String parentTaskId;         // PARENT_TASK_ID_ 示例: null (空)
    @Column(name = "DESCRIPTION_") private String description;          // DESCRIPTION_ 示例: null (空)
    @Column(name = "TASK_DEF_KEY_") private String taskDefKey;           // TASK_DEF_KEY_ 示例: "project001"
    @Column(name = "OWNER_") private String owner;                // OWNER_ 示例: null (空)
    @Column(name = "ASSIGNEE_") private String assignee;             // ASSIGNEE_ 示例: "$INITIATOR"
    @Column(name = "DELEGATION_") private String delegation;           // DELEGATION_ 示例: null (空)
    @Column(name = "PRIORITY_") private Integer priority;            // PRIORITY_ 示例: 50
    @Column(name = "CREATE_TIME_") private Timestamp createTime;        // CREATE_TIME_ 示例: "2024-09-23 15:17:28.654"
    @Column(name = "DUE_DATE_") private Date dueDate;                // DUE_DATE_ 示例: null (空)
    @Column(name = "CATEGORY_") private String category;             // CATEGORY_ 示例: null (空)
    @Column(name = "SUSPENSION_STATE_") private Integer suspensionState;     // SUSPENSION_STATE_ 示例: 1
    @Column(name = "TENANT_ID_") private String tenantId;             // TENANT_ID_ 示例: ""
    @Column(name = "FORM_KEY_") private String formKey;              // FORM_KEY_ 示例: null (空)
    @Column(name = "CLAIM_TIME_") private Date claimTime;              // CLAIM_TIME_ 示例: null (空)
    @Column(name = "IS_COUNT_ENABLED_") private Boolean isCountEnabled;      // IS_COUNT_ENABLED_ 示例: 1
    @Column(name = "VAR_COUNT_") private Integer varCount;            // VAR_COUNT_ 示例: 0
    @Column(name = "ID_LINK_COUNT_") private Integer idLinkCount;         // ID_LINK_COUNT_ 示例: 0
    @Column(name = "SUB_TASK_COUNT_") private Integer subTaskCount;        // SUB_TASK_COUNT_ 示例: 0
}
