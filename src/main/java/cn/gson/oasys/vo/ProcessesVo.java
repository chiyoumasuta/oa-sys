package cn.gson.oasys.vo;

import lombok.Data;

@Data
public class ProcessesVo {
    private String processInstanceId;       // 当前实例化流程ID
    private String processInstanceName;     // 实例化流程名称
    private String currentProgressId;       // 当前进度ID
    private String currentStep;             // 当前所在步骤
    private String businessDataId;          // 业务数据ID
    private String processInitiator;        // 流程发起人
    private String currentApprover;         // 当前流程审批用户
    private String lastApprovalTime;        // 上一次审核时间
    private String createTime;              // 创建时间
    private String processType;             // 流程类型

}
