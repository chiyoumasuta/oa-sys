package cn.gson.oasys.service;

import cn.gson.oasys.entity.Reimbursement;
import cn.gson.oasys.support.Page;

public interface ReimbursementService {
    /**
     * 创建申请工单
     * @param reimbursement
     * @return
     */
    Reimbursement saveOrUpdate(Reimbursement reimbursement);
    Page<Reimbursement> page(int pageNo, int pageSize,int searchType);

}
