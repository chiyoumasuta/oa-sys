package cn.gson.oasys.entity.reimbursement;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * 差旅费报销总表
 */
@Entity
@Table(name = "reimbursement_travel")
@Data
public class ReimbursementTravel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "reimbursement_id",columnDefinition = "关联总表")
    private int reimbursementId;
    @Column(name = "reporter", columnDefinition = "报销人")
    private String reporter;
    @Column(name = "reimbursement_date", columnDefinition = "报销日期")
    private Date reimbursementDate;
    @Column(name = "total_amount", columnDefinition = "费用总额(元)")
    private Double totalAmount;
    @Column(name = "travel_days", columnDefinition = "差旅时间(天)")
    private int travelDays;
    @Transient
    private List<ReimbursementTravelItem> items;
}
