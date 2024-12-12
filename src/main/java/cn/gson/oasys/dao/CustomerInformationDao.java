package cn.gson.oasys.dao;

import cn.gson.oasys.entity.CustomerInformation;
import org.flowable.bpmn.model.Interface;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface CustomerInformationDao extends Mapper<CustomerInformation> {
}
