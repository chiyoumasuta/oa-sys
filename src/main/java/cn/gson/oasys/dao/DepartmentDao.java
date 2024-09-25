package cn.gson.oasys.dao;

import cn.gson.oasys.entity.Department;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface DepartmentDao extends Mapper<Department> {
}
