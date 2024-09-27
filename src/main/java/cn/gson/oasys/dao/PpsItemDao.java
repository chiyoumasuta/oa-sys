package cn.gson.oasys.dao;

import cn.gson.oasys.entity.PpsItem;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface PpsItemDao extends Mapper<PpsItem> {
}
