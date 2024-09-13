package cn.gson.oasys.service;

import cn.gson.oasys.entity.config.SysConfig;

/**
 * 系统配置接口
 */
public interface SysConfigService {
    /**
     * 存储/修改配置信息
     */
    boolean save(SysConfig sysConfig);

    /**
     * 根据名称获取配置信息
     */
    SysConfig getSysConfig(String name);
}
