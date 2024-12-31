package cn.gson.oasys.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * 项目归档资料
 * 文件格式：文件ossId,上传人,上传时间
 */
@Entity
@Data
@Table(name = "project_archives")
public class ProjectArchives {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "project",columnDefinition = "项目名称")
    private String project;
    @Column(name = "create_user",columnDefinition = "创建人")
    private String createUser;
    @Column(name = "create_time",columnDefinition = "创建时间")
    private Date createTime;
    @Column(name = "extended_record_sheet",columnDefinition = "拓展记录表")
    private String extendedRecordSheet;
    @Column(name = "extended_record_sheet_person",columnDefinition = "拓展记录表编辑人")
    private String extendedRecordSheetPerson;
    @Column(name = "scheme",columnDefinition = "方案")
    private String scheme;
    @Column(name = "scheme_person",columnDefinition = "方案编辑人")
    private String schemePerson;
    @Column(name = "meeting_minutes",columnDefinition = "立项会议纪要")
    private String meetingMinutes;
    @Column(name = "meeting_minutes_person",columnDefinition = "立项会议纪要编辑人")
    private String meetingMinutesPerson;
    @Column(name = "bidding_documents",columnDefinition = "招投标文件（多个）")
    private String biddingDocuments;
    @Column(name = "bidding_documents_person",columnDefinition = "招投标文件编辑人")
    private String biddingDocumentsPerson;
    @Column(name = "contract",columnDefinition = "合同")
    private String contract;
    @Column(name = "contract_person",columnDefinition = "合同编辑人")
    private String contractPerson;
    @Column(name = "construction_materials",columnDefinition = "项目施工资料")
    private String constructionMaterials;
    @Column(name = "construction_materials_person",columnDefinition = "项目施工资料编辑人")
    private String constructionMaterialsPerson;
    @Column(name = "after_sales_information",columnDefinition = "售后资料，不参与审核，可在审核后多次上传")
    private String afterSalesInformation;
    @Column(name = "after_sales_information_person",columnDefinition = "售后资料编辑人")
    private String afterSalesInformationPerson;
    @Column(name = "audit_user",columnDefinition = "审核人")
    private Long auditUser;
    @Column(name = "audit_time",columnDefinition = "审核时间")
    private Date auditTime;
    @Column(name = "audit",columnDefinition = "是否审核")
    private boolean audit;
}