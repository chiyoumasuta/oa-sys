package cn.gson.oasys.service;

import cn.gson.oasys.entity.LeaveApplication;
import cn.gson.oasys.entity.reimbursement.Reimbursement;
import cn.gson.oasys.entity.reimbursement.ReimbursementItem;
import cn.gson.oasys.support.Page;

import java.util.Date;
import java.util.List;


public interface ReimbursementService {
    /**
     * 获取报销数据列表
     */
    Page<Reimbursement> getList(int pageSize, int pageNo, Date startDate, Date endDate, String project);

    /**
     * 实例化项目
     */
    boolean start(String deployId, String dateJson);

    /**
     * 获取业务代码信息
     */
    Reimbursement getInfo(Long id);

    /**
     * 审核接口
     */
     boolean audit(String id, String result);

    /**
     * 修改数据
     */
    void update(Reimbursement reimbursement);

    /**
     * 修改明细表数据
     */
    void updateItem(ReimbursementItem reimbursementItem);
}
