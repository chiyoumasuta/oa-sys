package cn.gson.oasys.dao;

import cn.gson.oasys.entity.Role;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface RoleDao extends Mapper<Role> {
}
