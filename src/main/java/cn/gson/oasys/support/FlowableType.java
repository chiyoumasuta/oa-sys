package cn.gson.oasys.support;

public enum FlowableType {

    PROJECT_PROCESS("项目管理"),
    LEAVE("请假审批");

    private String text;

    FlowableType(String text) {
        this.text = text;
    }
}
