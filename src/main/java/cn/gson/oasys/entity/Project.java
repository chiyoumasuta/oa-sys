package cn.gson.oasys.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * 项目维护表
 */
@Entity
@Table(name = "project")
@Data
public class Project {
    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "name",columnDefinition = "项目开始时间")
    private String name;
    @Column(name = "start_time",columnDefinition = "项目开始时间")
    private Date startTime;
    @Column(name = "end_time",columnDefinition = "项目结束时间")
    private Date endTime;
}
