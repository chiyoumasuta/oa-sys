package cn.gson.oasys.dao;

import cn.gson.oasys.entity.Project;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface ProjectDao extends Mapper<Project> {
}
