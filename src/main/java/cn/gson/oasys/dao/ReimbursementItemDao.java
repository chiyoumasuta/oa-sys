package cn.gson.oasys.dao;

import cn.gson.oasys.entity.reimbursement.ReimbursementItem;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface ReimbursementItemDao extends Mapper<ReimbursementItem> {
}
