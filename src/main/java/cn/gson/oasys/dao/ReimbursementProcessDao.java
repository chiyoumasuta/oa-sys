package cn.gson.oasys.dao;

import cn.gson.oasys.entity.reimbursement.ReimbursementProcess;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface ReimbursementProcessDao extends Mapper<ReimbursementProcess> {
}
