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
    private Object businessData;

    // 构造函数
    public TaskDTO(String id, String name, String taskDefinitionKey, 
                   String executionId, String processInstanceId) {
        this.id = id;
        this.name = name;
        this.taskDefinitionKey = taskDefinitionKey;
        this.executionId = executionId;
        this.processInstanceId = processInstanceId;
    }

    // Getter 和 Setter 方法
    // 可以根据需要添加
}
