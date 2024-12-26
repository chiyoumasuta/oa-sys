package cn.gson.oasys.service;

import cn.gson.oasys.entity.reimbursement.Reimbursement;
import cn.gson.oasys.entity.reimbursement.ReimbursementItem;
import cn.gson.oasys.support.Page;
import com.itextpdf.layout.Document;

import java.io.IOException;
import java.util.Date;


public interface ReimbursementService {
    /**
     * 获取报销数据列表
     */
    Page<Reimbursement> page(int pageSize, int pageNo, Date startDate, Date endDate, String project,int searchType);

    /**
     * 实例化项目
     */
    boolean start(String deployId, String dateJson);

    /**
     * 获取业务代码信息
     */
    Reimbursement getInfo(Long id,String searchType);

    /**
     * 审核接口
     */
     boolean audit(Long id,boolean isPass, String result);

    /**
     * 修改数据
     */
    void update(Reimbursement reimbursement);

    /**
     * 修改明细表数据
     */
    void updateItem(ReimbursementItem reimbursementItem);

    Reimbursement selectOneById(Long id);

    Document getDoc(Document doc, Reimbursement data) throws IOException;
}
