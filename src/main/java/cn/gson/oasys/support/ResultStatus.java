package cn.gson.oasys.support;

public enum ResultStatus {
    SUCCESS(200,"ok"),
    BAD_REQUEST(555,"no such data"),
    INTERNAL_SERVER_ERROR(500,"param fault"),
    REGISTER_FAIL(201,"exists"),
    NOT_LOGIN(401,"not login"),
    NO_PERMISSION(403,"No permission");
    private final Integer code;
    private final String msg;

    ResultStatus(int code, String msg){
        this.code=code;
        this.msg=msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public String toString() {
        return "ResultStatus{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}
