package cn.gson.oasys.dao;

import cn.gson.oasys.entity.config.SysConfig;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface SysConfigDao extends Mapper<SysConfig> {
}
