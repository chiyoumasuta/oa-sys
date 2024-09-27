package cn.gson.oasys.controller;


import cn.gson.oasys.entity.Pps;
import cn.gson.oasys.entity.PpsItem;
import cn.gson.oasys.service.PpsService;
import cn.gson.oasys.support.UtilResultSet;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/pps")
@Api(tags = "项目推进进度统计")
public class PpsController {
    @Resource
    private PpsService ppsService;

    @RequestMapping(value = "/page",method = RequestMethod.POST)
    @ApiOperation(value = "分页查询")
    public UtilResultSet page(int pageSize, int pageNo, String projectName){
        return UtilResultSet.success(ppsService.page(pageSize,pageNo,projectName));
    }

    @RequestMapping(value = "/saveOrUpdate",method = RequestMethod.POST)
    @ApiOperation(value = "添加/更新")
    public UtilResultSet saveOrUpdate(Pps pps){
        if (ppsService.saveOrUpdate(pps)){
            return UtilResultSet.success((pps.getId()==null?"添加":"修改")+"成功");
        }return UtilResultSet.bad_request((pps.getId()==null?"添加":"修改")+"失败");
    }

    @RequestMapping(value = "/saveItem",method = RequestMethod.POST)
    @ApiOperation(value = "添加进度")
    public UtilResultSet saveItem(PpsItem item){
        if (ppsService.saveItem(item)){
            return UtilResultSet.success("添加成功");
        }return UtilResultSet.bad_request("添加失败");
    }

    @RequestMapping(value = "/deleteItem",method = RequestMethod.POST)
    @ApiOperation(value = "通过id删除进度更新记录")
    public UtilResultSet deleteItem(Long id){
        if (ppsService.deleteItem(id)){
            return UtilResultSet.success("删除成功");
        }return UtilResultSet.bad_request("删除失败");
    }

    @RequestMapping(value = "/deletePps",method = RequestMethod.POST)
    @ApiOperation(value = "通过id删除项目进度统计")
    public UtilResultSet deletePps(Long id){
        if (ppsService.deletePps(id)){
            return UtilResultSet.success("删除成功");
        }return UtilResultSet.bad_request("删除失败");
    }
}
