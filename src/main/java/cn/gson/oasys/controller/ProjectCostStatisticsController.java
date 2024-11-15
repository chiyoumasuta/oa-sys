package cn.gson.oasys.controller;


import cn.gson.oasys.service.ProjectCostStatisticsService;
import cn.gson.oasys.support.UtilResultSet;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;

@RestController
@RequestMapping(value = "projectCostStatistics")
@Api(tags = "项目费用统计")
public class ProjectCostStatisticsController {

    @Resource
    private ProjectCostStatisticsService projectCostStatisticsService;

    @RequestMapping(value = "/group", method = RequestMethod.POST)
    @ApiOperation(value = "分类费用统计", notes = "group：项目(project)、职员(user)、部门(dept)")
    public UtilResultSet countByProject(Date startDate, Date endDate, String project, Long userId, String group) {
        switch (group) {
            case "project":
                return UtilResultSet.success(projectCostStatisticsService.countByProject(startDate, endDate, project));
            case "user":
                return UtilResultSet.success(projectCostStatisticsService.countByUser(startDate, endDate, userId, project));
            case "dept":
                return UtilResultSet.success(projectCostStatisticsService.countByDept(startDate, endDate, project));
            default:
                return UtilResultSet.bad_request("参数错误");
        }
    }
}
