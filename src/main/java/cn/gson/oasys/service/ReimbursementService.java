package cn.gson.oasys.service;

import cn.gson.oasys.entity.reimbursement.Reimbursement;


public interface ReimbursementService {
    /**
     * 实例化项目
     */
    boolean start(String deployId, String dateJson);

    /**
     * 获取业务代码信息
     */
    Reimbursement getInfo(Long id);
}
