package cn.gson.oasys.entity.project;

import lombok.Data;

import javax.persistence.*;

/**
 * 招投标和标书制作
 */
@Entity
@Table(name = "bid")
@Data
public class Bid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "header",columnDefinition = "项目负责人")
    private String header;
    @Column(name = "purchase_file",columnDefinition = "文件采购")
    private String purchaseFile;
    @Column(name = "bids",columnDefinition = "陪标信息")
    private String bids;
    @Column(name = "technology",columnDefinition = "标书-技术")
    private String technology;
    @Column(name = "商务",columnDefinition = "标书-商务")
    private String price;
    @Column(name = "price",columnDefinition = "标书-价格")
    private String price2;
    @Column(name = "bid_file",columnDefinition = "标书文件")
    private String bidFile;
    @Column(name = "pass",columnDefinition = "标书审核是否通过")
    private boolean pass;
}
