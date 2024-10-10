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

    @RequestMapping(value = "/countByProject", method = RequestMethod.POST)
    @ApiOperation("根据项目费用统计")
    public UtilResultSet countByProject(Date startDate, Date endDate, String project, String user) {
        return UtilResultSet.success(projectCostStatisticsService.countByProject(startDate, endDate, project, user));
    }

    @RequestMapping(value = "/countByUser", method = RequestMethod.POST)
    @ApiOperation("根据用户统计")
    public UtilResultSet countByUser(Date startDate, Date endDate, Long userId) {
        return UtilResultSet.success(projectCostStatisticsService.countByUser(startDate, endDate, userId));
    }
}
