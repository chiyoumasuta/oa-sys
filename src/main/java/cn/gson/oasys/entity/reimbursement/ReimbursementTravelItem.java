package cn.gson.oasys.entity.reimbursement;

import lombok.Data;

import javax.persistence.*;

/**
 * 差旅费报销明细
 */
@Entity
@Table(name = "reimbursement_travel_item")
@Data
public class ReimbursementTravelItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "reimbursement_travel_id",columnDefinition = "关联报销表")
    private Long reimbursement_id;
    @Column(name = "item_no", columnDefinition = "序号")
    private int itemNo;
    @Column(name = "item_name", columnDefinition = "项目名称")
    private String itemName;
    @Column(name = "item_cost_share", columnDefinition = "费用占比(元)")
    private Double itemCostShare;
    @Column(name = "item_description", columnDefinition = "说明")
    private String itemDescription;
}
