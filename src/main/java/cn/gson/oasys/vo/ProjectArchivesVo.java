package cn.gson.oasys.vo;

import cn.gson.oasys.entity.File;
import cn.gson.oasys.entity.User;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 项目归档资料
 * 文件格式：文件ossId,上传人,上传时间
 */
@Data
public class ProjectArchivesVo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String project;//项目名称
    private String createUser;//创建人
    private Date createTime;//创建时间
    private List<File> extendedRecordSheet;//拓展记录表
    private List<User> extendedRecordSheetPerson;
    private List<File> scheme;//方案
    private List<User> schemePerson;
    private List<File> meetingMinutes;//立项会议纪要
    private List<User> meetingMinutesPerson;
    private List<File> biddingDocuments;//招投标文件（多个）
    private List<User> biddingDocumentsPerson;
    private List<File> contract;//合同
    private List<User> contractPerson;
    private List<File> constructionMaterials;//项目施工资料
    private List<User> constructionMaterialsPerson;
    private List<File> afterSalesInformation;//售后资料
    private List<User> afterSalesInformationPerson;
    private String auditUserName;//审核人
    private Date auditTime;//审核时间
    private boolean audit;//是否审核
}
