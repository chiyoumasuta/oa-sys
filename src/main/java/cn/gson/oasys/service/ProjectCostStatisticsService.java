package cn.gson.oasys.service;

import cn.gson.oasys.vo.ProjectCostStatisticsVo;

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
    List<ProjectCostStatisticsVo> countByProject(Date startDate, Date endDate, String project, String user);

    /**
     * 更具用户名返回费用统计结果
     */
    Map<String,Map<String,Double>> countByUser(Date startDate, Date endDate, Long userId);
}
