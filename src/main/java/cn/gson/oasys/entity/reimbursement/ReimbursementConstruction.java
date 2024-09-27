package cn.gson.oasys.entity.reimbursement;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * 施工费用报销
 */

@Entity
@Table(name = "reimbursement_construction")
public class ReimbursementConstruction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "project_name", nullable = false)
    private String projectName;
    @Column(name = "record_date", nullable = false)
    private Date recordDate;
    @Column(name = "recorder", nullable = false)
    private Long recorder;
    @Column(name = "total_amount", columnDefinition = "费用总额(元)")
    private Double totalAmount;
    @Transient //明细表
    private List<ReimbursementConstructionItem> participants;
}