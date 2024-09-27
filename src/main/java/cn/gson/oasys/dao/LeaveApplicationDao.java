package cn.gson.oasys.dao;

import cn.gson.oasys.entity.LeaveApplication;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface LeaveApplicationDao extends Mapper<LeaveApplication> {
}
