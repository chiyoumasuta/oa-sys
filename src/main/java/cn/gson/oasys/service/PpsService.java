package cn.gson.oasys.service;

import cn.gson.oasys.entity.Pps;
import cn.gson.oasys.entity.PpsItem;
import cn.gson.oasys.support.Page;

import java.util.List;

/**
 * 项目推进统计
 */
public interface PpsService {
    /**
     * 分页查询
     */
    Page<Pps> page(int pageSize, int pageNo, String projectName);

    /**
     * 添加/更新
     */
    boolean saveOrUpdate(Pps pps);

    /**
     * 添加进度
     */
    boolean saveItem(PpsItem item);

    /**
     * 通过id删除进度更新
     */
    boolean deleteItem(Long id);

    /**
     * 通过id删除项目进度统计
     */
    boolean deletePps(Long id);
    /**
     * 查询进度详情列表
     */
    List<PpsItem> findByPpsId(Long id);

    /**
     * 上传方案/报告
     */
    boolean updateFile(Long id,Long fileId,int type);
}
