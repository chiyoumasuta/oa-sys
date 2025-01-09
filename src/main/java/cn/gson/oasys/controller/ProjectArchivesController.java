package cn.gson.oasys.controller;

import cn.gson.oasys.entity.ProjectArchives;
import cn.gson.oasys.service.ProjectArchivesService;
import cn.gson.oasys.support.UtilResultSet;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/projectArchives")
@Api(tags = "项目归档资料管理")
public class ProjectArchivesController {

    @Resource
    private ProjectArchivesService projectArchivesService;

    @RequestMapping(value = "add",method = RequestMethod.POST)
    @ApiOperation(value = "新增项目")
    public UtilResultSet add(ProjectArchives projectArchives){
        if (projectArchivesService.add(projectArchives)){
            return UtilResultSet.success("新增成功");
        }return UtilResultSet.bad_request("新增失败");
    }

    @RequestMapping(value = "update",method = RequestMethod.POST)
    @ApiOperation(value = "修改")
    public UtilResultSet update(Long id,String key,String value,boolean type){
        if (projectArchivesService.update(id, key, value, type)) {
            return UtilResultSet.success("修改成功");
        }else return UtilResultSet.bad_request("修改失败");
    }

    @RequestMapping(value = "delete",method = RequestMethod.POST)
    @ApiOperation(value = "删除，仅限创建人删除")
    public UtilResultSet delete(Long id){
        if (projectArchivesService.delete(id)){
            return UtilResultSet.success("删除成功");
        }else return UtilResultSet.bad_request( "删除失败");
    }

    @RequestMapping(value = "page",method = RequestMethod.POST)
    @ApiOperation(value = "分页查询")
    public UtilResultSet page(Integer pageNo, Integer pageSize, String project, Long id){
        return UtilResultSet.success(projectArchivesService.page(pageNo,pageSize,project,id));
    }

    @RequestMapping(value = "audit",method = RequestMethod.POST)
    @ApiOperation(value = "分页查询")
    public UtilResultSet audit(Long id){
        if (projectArchivesService.audit(id)){
            return UtilResultSet.success("审核通过");
        }else return UtilResultSet.bad_request( "审核不通过");
    }
}
