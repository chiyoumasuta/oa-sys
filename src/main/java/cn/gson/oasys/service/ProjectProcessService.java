package cn.gson.oasys.service;

import cn.gson.oasys.entity.config.ProjectProcessConfig;
import cn.gson.oasys.entity.ProjectProcess;
import cn.gson.oasys.support.Page;
import cn.gson.oasys.vo.ProjectProcessVo;

public interface ProjectProcessService {
    /**
     * 新建项目并实例化流程
     */
    boolean createProject(ProjectProcess projectProcess,String deployId,String dataJson);

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

    /**
     * 审核
     */
    Boolean taskByAssignee(String taskId,ProjectProcess projectProcess,String nextReviewer,Long fileId,String presentation);
}
