package cn.gson.oasys.controller;


import cn.gson.oasys.service.ProjectCostStatisticsService;
import cn.gson.oasys.support.ExcelUtil;
import cn.gson.oasys.support.UtilResultSet;
import cn.gson.oasys.vo.CostVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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

    @RequestMapping(value = "/download")
    @ApiOperation(value = "下载数据")
    public void download(Date startDate, Date endDate, String project, String type, HttpServletResponse response) {
        List<CostVo> lists = projectCostStatisticsService.getCostVoList(startDate,endDate,project,type);
        String[] heads = new String[]{"项目","人员","费用明细","金额","天数","备注"};
        String[] fields = new String[]{"project","person","costDetail","cost","days","remark"};
        List<String[]> rows = ExcelUtil.getRows(fields, lists);
        try {
            String fileName = "报销导出-" + (startDate==null?"":startDate)+ (endDate==null?"":endDate)+ ".xlsx";
            response.setHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes(StandardCharsets.UTF_8), Charset.forName("iso8859-1")));
            response.setContentType("application/octet-stream;charset=utf8");
            ExcelUtil.download(null, heads, rows, response.getOutputStream());
        } catch (Exception ignored) {
        }
    }
}
