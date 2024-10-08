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
    private Double  implementation; //项目实施费用统计
    private String implementationName; //项目实施参与人
    private Map<String,Double> statistics; //差旅费分类统计
    private Map<String,Double> detailsByUser; //按报销人统计
    private List<ReimbursementItem> costDetails; //详细费用

    @Data
    public static class detail{
        private String userName; //报销人
        private String type; //报销类型
        private String info; //详细信息
        private Double cost; //费用
        private Date createTime; //创建时间
    }
}