package cn.gson.oasys.service;

import cn.gson.oasys.entity.config.ProjectProcessConfig;
import cn.gson.oasys.entity.project.ProjectProcess;
import cn.gson.oasys.support.Page;
import cn.gson.oasys.vo.ProjectProcessVo;

public interface ProjectProcessService {
    /**
     * 新建项目
     */
    boolean createProject(ProjectProcess projectProcess);

    /**
     * 获取项目列表
     */
    Page<ProjectProcess> page(int pageNo, int pageSize,String name);

    /**
     * 查看项目列表详情
     */
    ProjectProcessVo getInfo(Long id);


    /**
     * 获取配置信息
     */
    ProjectProcessConfig getConfig();
}
