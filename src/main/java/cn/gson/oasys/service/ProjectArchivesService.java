package cn.gson.oasys.service;

import cn.gson.oasys.entity.ProjectArchives;
import cn.gson.oasys.support.Page;
import cn.gson.oasys.vo.ProjectArchivesVo;

public interface ProjectArchivesService {
    /**
     * 添加
     */
    boolean add(ProjectArchives projectArchives);

    /**
     * 修改/删除数据
     * @param id id
     * @param key 字段名
     * @param value 字段类型
     * @param type 操作类型 true 删除 false 修改
     * @return 是否操作成功
     */
    boolean update(Long id,String key,String value,boolean type);

    /**
     * 删除数据
    */
    boolean delete(Long id);

    /**
     * 分页查询数据
     */
    Page<ProjectArchivesVo> page(Integer pageNo, Integer pageSize, String project, Long id);

    boolean audit(Long id);
}
