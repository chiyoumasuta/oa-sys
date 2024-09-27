package cn.gson.oasys.dao;

import cn.gson.oasys.entity.reimbursement.ReimbursementTravel;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface ReimbursementTravelDao extends Mapper<ReimbursementTravel> {
}
