package cn.gson.oasys.entity;

import cn.gson.oasys.entity.reimbursement.Reimbursement;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 报销明细类型维护
 */
@Entity
@Table(name = "rei_type")
@Data
public class ReiType {
    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "name", columnDefinition = "名称")
    private String name;
    @Column(name = "type", columnDefinition = "所属报销类型")
    private Reimbursement.ExpenseType type;
}
