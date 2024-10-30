package cn.gson.oasys.support;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class UtilResultSet extends JSONObject {
    private static final long serialVersionUID = 1L;
    private static final String CODE_TAG="code";
    private static final String MSG_TAG="msg";
    private static final String DATA_TAG="list";

    UtilResultSet(){}

    public UtilResultSet(ResultStatus resultStatus) {
        super.put(CODE_TAG, resultStatus.getCode());
        super.put(MSG_TAG, resultStatus.getMsg());
    }
    public UtilResultSet(ResultStatus resultStatus, Object list) {
        super.put(CODE_TAG, resultStatus.getCode());
        super.put(MSG_TAG, resultStatus.getMsg());
        if(list!=null) {
            super.put(DATA_TAG,list);
        }
    }

    public static UtilResultSet success(Object list) {
        return new UtilResultSet(ResultStatus.SUCCESS,list);
    }
    public static UtilResultSet register_fail() {
        return new UtilResultSet(ResultStatus.REGISTER_FAIL);
    }

    public static UtilResultSet bad_request(Object list) {
        return new UtilResultSet(ResultStatus.BAD_REQUEST,list);
    }

    public static UtilResultSet not_login(Object list) {
        return new UtilResultSet(ResultStatus.NOT_LOGIN,list);
    }

    public static UtilResultSet internal_server_error() {
        return new UtilResultSet(ResultStatus.INTERNAL_SERVER_ERROR);
    }
    public static UtilResultSet no_permission() {
        return new UtilResultSet(ResultStatus.NO_PERMISSION, new ArrayList<>());
    }
}
