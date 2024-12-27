package cn.gson.oasys.dao;

import cn.gson.oasys.entity.Permissions;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface PermissionsDao extends Mapper<Permissions> {
}
