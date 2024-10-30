package cn.gson.oasys.vo;

import cn.gson.oasys.entity.reimbursement.ReimbursementItem;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 项目费用统计
 */
@Data
public class ProjectCostStatisticsVo {
    private String projectName; //项目名称
    private Double totalCost; //总费用
    private Double implementation; //项目实施费用统计
    private Double implementationDay; //项目实施总天数
    private Map<String,Double> implementationDetail; //项目参与人和参与天数明细
    private Map<String,Double> statistics; //差旅费分类统计
    private Map<String,Map<String,Double>> detailsByUser; //按报销人统计
    private List<ReimbursementItem> costDetails; //详细费用
    private List<ProjectCostStatisticsVo> childrenList;
}