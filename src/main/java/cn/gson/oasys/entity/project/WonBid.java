package cn.gson.oasys.entity.project;

import lombok.Data;

import javax.persistence.*;

/**
 * 中标后流程
 */
@Entity
@Table(name = "won_bid")
@Data
public class WonBid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
