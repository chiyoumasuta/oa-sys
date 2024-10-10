package cn.gson.oasys.entity.flowable;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "ACT_DE_MODEL")
@Data
public class ActDeModel {

    @Id
    @Column(name = "id")
    private String id;
    @Column(name = "name")
    private String name;
    @Column(name = "model_key")
    private String modelKey;
    @Column(name = "description")
    private String description;
    @Column(name = "model_comment")
    private String modelComment;
    @Column(name = "created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    @Column(name = "created_by")
    private String createdBy;
    @Column(name = "last_updated")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdated;
    @Column(name = "last_updated_by")
    private String lastUpdatedBy;
    @Column(name = "version")
    private Integer version;
    @Column(name = "model_editor_json", columnDefinition = "LONGTEXT")
    private String modelEditorJson;
    @Column(name = "thumbnail")
    private byte[] thumbnail;
    @Column(name = "model_type")
    private Integer modelType;
    @Column(name = "tenant_id")
    private String tenantId;
}