package cn.gson.oasys.service;

import cn.gson.oasys.vo.CostVo;
import cn.gson.oasys.vo.ProjectCostStatisticsVo;
import javafx.beans.binding.DoubleExpression;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 费用统计接口
 */
public interface ProjectCostStatisticsService {

    /**
     * 根据项目返回费用统计结果
     */
    List<ProjectCostStatisticsVo> countByProject(Date startDate, Date endDate, String project);

    /**
     * 根据用户名返回费用统计结果
     */
    Map<String, Map<String, Double>> countByUser(Date startDate, Date endDate, Long userId, String project);

    /**
     * 根据部门分类
     */
    Map<String, Map<String, Double>> countByDept(Date startDate, Date endDate, String project);

    /**
     * 导出
     */
    List<CostVo> getCostVoList(String startDate, String endDate, String project,String type);

}
