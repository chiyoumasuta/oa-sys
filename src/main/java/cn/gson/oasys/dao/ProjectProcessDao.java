package cn.gson.oasys.dao;

import cn.gson.oasys.entity.ProjectProcess;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface ProjectProcessDao extends Mapper<ProjectProcess> {
}