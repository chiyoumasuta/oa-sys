package cn.gson.oasys.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class TaskDTO implements Serializable {
    private String id;
    private String name;
    private String taskDefinitionKey;
    private String executionId;
    private String processInstanceId;
    private Long businessKey;
    private Object businessData;

    // 构造函数
    public TaskDTO(String id, String name, String taskDefinitionKey, 
                   String executionId, String processInstanceId,Long businessKey) {
        this.id = id;
        this.name = name;
        this.taskDefinitionKey = taskDefinitionKey;
        this.executionId = executionId;
        this.processInstanceId = processInstanceId;
        this.businessKey = businessKey;
    }
}
