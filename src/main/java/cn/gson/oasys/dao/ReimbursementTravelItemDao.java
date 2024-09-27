package cn.gson.oasys.dao;

import cn.gson.oasys.entity.reimbursement.ReimbursementTravelItem;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface ReimbursementTravelItemDao extends Mapper<ReimbursementTravelItem> {
}
