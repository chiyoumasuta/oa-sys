package cn.gson.oasys.service;

import cn.gson.oasys.entity.Project;
import cn.gson.oasys.entity.ReiType;
import cn.gson.oasys.entity.config.SysConfig;
import cn.gson.oasys.vo.SysConfigListVo;

import java.util.List;

/**
 * 系统配置接口
 */
public interface SysConfigService {
    /**
     * 存储/修改配置信息
     */
    boolean saveOrUpdate(SysConfig sysConfig);

    /**
     * 根据名称获取配置信息
     */
    SysConfig getSysConfig(String name);

    /**
     * 获取所有请假申请配置
     */
    List<SysConfig> getLeaveConfig();

    /**
     * 获取配置信息
     * @return 列表
     */
    List<SysConfigListVo> getSysConfigByList(String name);

    /**
     * 获取项目列表
     */
    List<Project> getProjectList();

    /**
     * 删除项目
     */
    boolean deleteProject(Long id);

    /**
     * 增加/修改项目列表
     */
    boolean saveOrUpdate(Project project);

    /**
     * 获取报销明细类型配置列表
     */
    List<ReiType> getReiTypeList(String type);

    /**
     * 删除报销明细类型
     */
    boolean deleteReiType(Long id);

    /**
     * 增加/修改报销明细类型列表
     */
    boolean saveOrUpdate(ReiType reiType);
}
