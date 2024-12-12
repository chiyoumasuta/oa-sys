package cn.gson.oasys.vo;

import lombok.Data;

@Data
public class CostVo {
    private String project; //项目
    private String person; //人员
    private String costDetail; //费用明细
    private Double cost; //金额
    private Double days; //天数
    private String remark; //备注
}
