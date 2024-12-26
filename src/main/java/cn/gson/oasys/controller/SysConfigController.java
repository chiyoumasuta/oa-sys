package cn.gson.oasys.controller;

import cn.gson.oasys.entity.Project;
import cn.gson.oasys.entity.ReiType;
import cn.gson.oasys.entity.config.SysConfig;
import cn.gson.oasys.service.SysConfigService;
import cn.gson.oasys.support.UtilResultSet;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 系统配置
 */
@RestController
@RequestMapping("/sys")
@Api(tags = "系统配置")
public class SysConfigController {

    @Resource
    private SysConfigService sysConfigService;

    @RequestMapping(value = "/saveOrUpdate", method = RequestMethod.POST)
    @ApiOperation(value = "更新/添加配置(sys_config)")
    public UtilResultSet saveOrUpdate(SysConfig config) {
        if (sysConfigService.saveOrUpdate(config)) {
            return UtilResultSet.success("更新/添加成功");
        }
        return UtilResultSet.bad_request("更新/添加失败");
    }

    @RequestMapping(value = "/getByName", method = RequestMethod.POST)
    @ApiOperation(value = "根据名称获取配置")
    public UtilResultSet getByName(String name) {
        return UtilResultSet.success(sysConfigService.getSysConfig(name));
    }

    @RequestMapping(value = "/getLeaveConfig", method = RequestMethod.POST)
    @ApiOperation(value = "获取所有请假审批配置")
    public UtilResultSet getLeaveConfig() {
        return UtilResultSet.success(sysConfigService.getLeaveConfig());
    }

    @RequestMapping(value = "/getListByName", method = RequestMethod.POST)
    @ApiOperation(value = "根据名称获取配置list")
    public UtilResultSet getListByName(String name) {
        return UtilResultSet.success(sysConfigService.getSysConfigByList(name));
    }

    @RequestMapping(value = "/getProjectList", method = RequestMethod.POST)
    @ApiOperation(value = "获取项目配置表")
    public UtilResultSet getProjectList() {
        return UtilResultSet.success(sysConfigService.getProjectList());
    }

    @RequestMapping(value = "/deleteProject", method = RequestMethod.POST)
    @ApiOperation(value = "删除项目")
    public UtilResultSet deleteProject(Long id) {
        if (sysConfigService.deleteProject(id)) {
            return UtilResultSet.success("删除成功");
        }
        return UtilResultSet.bad_request("删除失败");
    }

    @RequestMapping(value = "/saveOrUpdateProject", method = RequestMethod.POST)
    @ApiOperation(value = "添加/更新项目(project)")
    public UtilResultSet saveOrUpdate(Project project) {
        sysConfigService.saveOrUpdate(project);
        return UtilResultSet.success("添加/修改成功");
    }

    @RequestMapping(value = "/getReiTypeList", method = RequestMethod.POST)
    @ApiOperation(value = "获取报销明细类型配置")
    public UtilResultSet getReiTypeList(String type) {
        return UtilResultSet.success(sysConfigService.getReiTypeList(type));
    }

    @RequestMapping(value = "/deleteReiType", method = RequestMethod.POST)
    @ApiOperation(value = "删除报销明细配置")
    public UtilResultSet deleteReiType(Long id) {
        if (sysConfigService.deleteReiType(id)) {
            return UtilResultSet.success("删除成功");
        }
        return UtilResultSet.bad_request("删除失败");
    }

    @RequestMapping(value = "/saveOrUpdateReiType", method = RequestMethod.POST)
    @ApiOperation(value = "添加/修改报销费用明细类型")
    public UtilResultSet saveOrUpdate(ReiType reiType) {
        sysConfigService.saveOrUpdate(reiType);
        return UtilResultSet.success("添加/修改成功");
    }
}
