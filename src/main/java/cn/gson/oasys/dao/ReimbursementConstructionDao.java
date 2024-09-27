package cn.gson.oasys.dao;

import cn.gson.oasys.entity.reimbursement.ReimbursementConstruction;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import javax.annotation.Resource;

@Repository
public interface ReimbursementConstructionDao extends Mapper<ReimbursementConstruction> {
}
