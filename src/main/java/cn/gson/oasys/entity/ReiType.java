package cn.gson.oasys.entity;

import cn.gson.oasys.entity.reimbursement.Reimbursement;
import lombok.Data;

import javax.persistence.*;

/**
 * 报销明细类型维护
 */
@Entity
@Table(name = "rei_type")
@Data
public class ReiType {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", columnDefinition = "名称")
    private String name;
    @Column(name = "type", columnDefinition = "所属报销类型")
    private Reimbursement.ExpenseType type;
}
