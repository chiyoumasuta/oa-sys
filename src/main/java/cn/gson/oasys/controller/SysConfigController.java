package cn.gson.oasys.controller;

import cn.gson.oasys.entity.config.SysConfig;
import cn.gson.oasys.service.SysConfigService;
import cn.gson.oasys.support.UtilResultSet;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @RequestMapping(value = "/saveOrUpdate")
    @ApiOperation(value = "更新/添加配置")
    public UtilResultSet saveOrUpdate(SysConfig config) {
        if (sysConfigService.save(config)){
            return UtilResultSet.success("更新/添加成功");
        }return UtilResultSet.bad_request("更新/添加失败");
    }

    @RequestMapping(value = "/getBuName")
    @ApiOperation(value = "根据名称获取配置")
    public UtilResultSet getBuName(String name) {
        return UtilResultSet.success(sysConfigService.getSysConfig(name));
    }
}
