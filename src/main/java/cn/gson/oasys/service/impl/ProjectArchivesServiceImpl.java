package cn.gson.oasys.service.impl;

import cn.gson.oasys.dao.ProjectArchivesDao;
import cn.gson.oasys.entity.ProjectArchives;
import cn.gson.oasys.service.ProjectArchivesService;
import cn.gson.oasys.support.Page;
import cn.gson.oasys.support.UserTokenHolder;
import cn.gson.oasys.support.exception.ServiceException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Field;

@Service
public class ProjectArchivesServiceImpl implements ProjectArchivesService {

    @Resource
    private ProjectArchivesDao projectArchivesDao;

    @Override
    public boolean add(ProjectArchives projectArchives) {
        return projectArchivesDao.insert(projectArchives) > 0;
    }

    @Override
    public boolean update(Long id, String key, String value, boolean type) {
        ProjectArchives archives = projectArchivesDao.selectByPrimaryKey(id);

        try {
            Field field = ProjectArchives.class.getDeclaredField(key);
            field.setAccessible(true);
            field.set(archives, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(Long id) {
        ProjectArchives projectArchives = projectArchivesDao.selectByPrimaryKey(id);
        if (!projectArchives.getAuditUser().equals(UserTokenHolder.getUser().getId())) {
            throw new ServiceException("只有创建人能修改");
        }
        return false;
    }

    @Override
    public Page<ProjectArchives> page(Integer pageNo, Integer pageSize, String project) {
        return null;
    }
}
