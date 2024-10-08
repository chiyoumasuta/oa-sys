package cn.gson.oasys.controller;

import cn.gson.oasys.entity.ProjectProcess;
import cn.gson.oasys.support.FlowableApiUtils;
import cn.gson.oasys.service.ProjectProcessService;
import cn.gson.oasys.support.UserTokenHolder;
import cn.gson.oasys.support.UtilResultSet;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/projectProcess")
@Api(tags = "项目标准化流程相关接口")
public class ProjectProcessController {
    @Resource
    private ProjectProcessService projectProcessService;
    @Resource
    private FlowableApiUtils flowableApiUtils;

    @RequestMapping(value = "/createProject",method = RequestMethod.POST)
    @ApiOperation(value = "创建")
    public UtilResultSet createProject(ProjectProcess projectProcess,String deployId,String dataJson){
        if (projectProcessService.createProject(projectProcess,deployId,dataJson)){
            return UtilResultSet.success("添加成功");
        }else return UtilResultSet.bad_request("添加失败");
    }

    @RequestMapping(value = "/page",method = RequestMethod.POST)
    @ApiOperation(value = "获取需要审核的业务列表")
    public UtilResultSet page(int pageNo, int pageSize, String name){
        return UtilResultSet.success(flowableApiUtils.getRuntimeBusinessKeyByUser(String.valueOf(UserTokenHolder.getUser().getId()),"项目管理"));
    }

    @RequestMapping(value = "/getInfo",method = RequestMethod.POST)
    @ApiOperation(value = "获取详情")
    public UtilResultSet getInfo(Long id){
       return UtilResultSet.success(projectProcessService.getInfo(id));
    }

    @RequestMapping(value = "/getConfig",method = RequestMethod.POST)
    @ApiOperation(value = "获取配置信息")
    public UtilResultSet getConfig(){
        return UtilResultSet.success(projectProcessService.getConfig());
    }
}
