package cn.gson.oasys.entity;

import java.util.Date;
import java.util.List;

import javax.persistence.*;

import lombok.Data;


@Entity
@Table(name = "file")
@Data
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id", columnDefinition = "文件id")
    private Long fileId;
    @Column(name = "file_name", columnDefinition = "文件名")
    private String fileName;
    @Column(name = "file_path", columnDefinition = "文件路径")
    private String filePath;
    @Column(name = "father", columnDefinition = "上级")
    private Long father;
    @Column(name = "size", columnDefinition = "文件大小")
    private Long size;
    @Column(name = "content_type", columnDefinition = "文件类型id")
    private String contentType;
    @Column(name = "upload_time", columnDefinition = "上传时间")
    private Date uploadTime;
    @Column(name = "model", columnDefinition = "所属模块:")
    private model model;
    @Column(name = "type", columnDefinition = "文件类型（文件夹：folder ，文件：Doc、docx、xls、xlsx、ppt、pptx、pdf、txt、rtf、odt、ods、odp、jpg)")
    private String type;
    @Column(name = "status", columnDefinition = "审核状态0：无需审核 1：待审核 2:审核通过 3:驳回")
    private int status;
    @Column(name = "is_share", columnDefinition = "是否共享")
    private boolean isShare;
    @Column(name = "share_people", columnDefinition = "共享用户")
    private String sharePeople;
    @Column(name = "file_in_trash", columnDefinition = "是否在回收站")
    private boolean fileInTrash;
    @Column(name = "user_id", columnDefinition = "所属用户")
    private Long userId;
    @Transient
    private List<File> files;

    //临时数据
    @Transient
    private User user;

    //文件类型
    public enum model {
        CLOUD("云盘"),
        REIMBURSEMENT("报销文件");

        private final String text;

        model(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }
}
