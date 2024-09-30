package cn.gson.oasys.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 项目费用统计
 */
@Data
public class ProjectCostStatisticsVo {
    private String projectName; //项目名称
    private Double totalCost; //总费用
    private List<detail> statistics; //分类统计
    private List<detail> costDetails; //详细费用

    @Data
    public class detail{
        private String userName; //报销人
        private String type; //报销类型
        private String info; //xiang
        private String cost;
        private Date createTime;
    }
}