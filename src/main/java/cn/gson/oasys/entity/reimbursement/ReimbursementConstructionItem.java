package cn.gson.oasys.entity.reimbursement;

import javax.persistence.*;

/**
 * 施工费用详情
 */

@Entity
@Table(name = "reimbursement_construction_item")
public class ReimbursementConstructionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reimbursement_construction_id", nullable = false)
    private Long reimbursementConstructionId;

    @Column(name = "participant_name", nullable = false)
    private String participantName;

    @Column(name = "participation_days", nullable = false)
    private int participationDays;

    @Column(name = "description")
    private String description;
}