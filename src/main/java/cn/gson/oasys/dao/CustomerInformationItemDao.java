package cn.gson.oasys.dao;

import cn.gson.oasys.entity.CustomerInformationItem;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface CustomerInformationItemDao extends Mapper<CustomerInformationItem> {
}
