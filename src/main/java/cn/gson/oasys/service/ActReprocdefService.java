package cn.gson.oasys.service;

import cn.gson.oasys.entity.flowable.ActReProcdef;

import java.util.List;

public interface ActReprocdefService {
    /**
     * 获取列表
     */
    List<ActReProcdef> getActReprocdef();

    /**
     * 更具名称删除数据
     */
    boolean deleteByName(String name);
}
