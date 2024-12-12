package cn.gson.oasys.vo;

import cn.gson.oasys.entity.reimbursement.ReimbursementItem;
import lombok.Data;

import java.util.*;

/**
 * 项目费用统计
 */
@Data
public class ProjectCostStatisticsVo {
    private String projectName; //项目名称
    private Double totalCost = 0.0; //总费用
    private Double implementation = 0.0; //项目实施费用统计
    private Double implementationDay = 0.0; //项目实施总天数
    private Double amortization = 0.0; //摊销费用
    private Map<String,Double> implementationDayDetail = new HashMap<>(); //项目参与人和参与天数明细
    private Map<String,Double> implementationDetail = new HashMap<>(); //实施费用分类统计
    private Map<String,Double> statistics = new HashMap<>(); //差旅费分类统计
    private Map<String,Map<String,Double>> detailsByUser = new HashMap<>(); //按报销人统计
    private List<ReimbursementItem> costDetails = new ArrayList<>(); //详细费用
    private List<ProjectCostStatisticsVo> childrenList = new ArrayList<>();
}