package cn.gson.oasys.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "act_re_procdef")
@Data
public class ActReProcdef {
    @Id
    @Column(name = "ID_")
    private String id;
    @Column(name = "REV_")
    private Integer rev;
    @Column(name = "CATEGORY_")
    private String category;
    @Column(name = "NAME_")
    private String name;
    @Column(name = "KEY_")
    private String key;
    @Column(name = "VERSION_")
    private Integer version;
    @Column(name = "DEPLOYMENT_ID_")
    private String deploymentId;
    @Column(name = "RESOURCE_NAME_")
    private String resourceName;
    @Column(name = "DGRM_RESOURCE_NAME_")
    private String dgrmResourceName;
    @Column(name = "DESCRIPTION_")
    private String description;
    @Column(name = "HAS_START_FORM_KEY_")
    private Boolean hasStartFormKey;
    @Column(name = "HAS_GRAPHICAL_NOTATION_")
    private Boolean hasGraphicalNotation;
    @Column(name = "SUSPENSION_STATE_")
    private Integer suspensionState;
    @Column(name = "TENANT_ID_")
    private String tenantId;
    @Column(name = "ENGINE_VERSION_")
    private String engineVersion;
    @Column(name = "DERIVED_FROM_")
    private String derivedFrom;
    @Column(name = "DERIVED_FROM_ROOT_")
    private String derivedFromRoot;
    @Column(name = "DERIVED_VERSION_")
    private Integer derivedVersion;
}