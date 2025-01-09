package cn.gson.oasys.service.impl;

import cn.gson.oasys.dao.ProjectArchivesDao;
import cn.gson.oasys.entity.File;
import cn.gson.oasys.entity.ProjectArchives;
import cn.gson.oasys.entity.User;
import cn.gson.oasys.service.FileService;
import cn.gson.oasys.service.ProjectArchivesService;
import cn.gson.oasys.service.UserService;
import cn.gson.oasys.support.Page;
import cn.gson.oasys.support.UserTokenHolder;
import cn.gson.oasys.support.exception.ServiceException;
import cn.gson.oasys.vo.ProjectArchivesVo;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectArchivesServiceImpl implements ProjectArchivesService {

    @Resource
    private ProjectArchivesDao projectArchivesDao;
    @Resource
    private UserService userService;
    @Resource
    private FileService fileService;

    @Override
    public boolean add(ProjectArchives projectArchives) {
        projectArchives.setCreateUser(UserTokenHolder.getUser().getId());
        projectArchives.setCreateTime(new Date());
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
        return projectArchivesDao.updateByPrimaryKeySelective(archives) > 0;
    }

    @Override
    public boolean delete(Long id) {
        ProjectArchives projectArchives = projectArchivesDao.selectByPrimaryKey(id);
        if (!projectArchives.getCreateUser().equals(UserTokenHolder.getUser().getId())) {
            throw new ServiceException("只有创建人能修改");
        }
        projectArchives.setDelete(true);
        return projectArchivesDao.updateByPrimaryKeySelective(projectArchives) > 0;
    }

    @Override
    public Page<ProjectArchivesVo> page(Integer pageNo, Integer pageSize, String project, Long id) {
        PageHelper.startPage(pageNo, pageSize);
        Example example = new Example(ProjectArchives.class);
        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(project)) {
            criteria.andEqualTo("project", project);
        }
        if (id!=null){
            criteria.andEqualTo("id", id);
        }
        criteria.andEqualTo("delete",false);
        com.github.pagehelper.Page<ProjectArchives> page = (com.github.pagehelper.Page<ProjectArchives>) projectArchivesDao.selectByExample(example);
        List<ProjectArchivesVo> result = page.getResult().stream().map(it->{
            ProjectArchivesVo vo = new ProjectArchivesVo();
            vo.setId(it.getId());
            vo.setProject(it.getProject());
            vo.setAuditTime(it.getAuditTime());
            vo.setAudit(it.isAudit());
            vo.setCreateUser(userService.findById(it.getCreateUser()).getUserName());
            vo.setCreateTime(it.getCreateTime());
            vo.setAuditUserName(userService.findById(it.getAuditUser()).getUserName());
            vo.setExtendedRecordSheet(fileService.findByIds(it.getExtendedRecordSheet()));//拓展记录表
            vo.setExtendedRecordSheetPerson(userService.findByIds(it.getExtendedRecordSheetPerson()));
            vo.setScheme(fileService.findByIds(it.getScheme()));//方案
            vo.setSchemePerson(userService.findByIds(it.getSchemePerson()));
            vo.setMeetingMinutes(fileService.findByIds(it.getMeetingMinutes()));//立项会议纪要
            vo.setMeetingMinutesPerson(userService.findByIds(it.getMeetingMinutesPerson()));
            vo.setBiddingDocuments(fileService.findByIds(it.getBiddingDocuments()));//招投标文件（多个）
            vo.setBiddingDocumentsPerson(userService.findByIds(it.getBiddingDocumentsPerson()));
            vo.setContract(fileService.findByIds(it.getContract()));//合同
            vo.setContractPerson(userService.findByIds(it.getContractPerson()));
            vo.setConstructionMaterials(fileService.findByIds(it.getConstructionMaterials()));//项目施工资料
            vo.setConstructionMaterialsPerson(userService.findByIds(it.getConstructionMaterialsPerson()));
            vo.setAfterSalesInformation(fileService.findByIds(it.getAfterSalesInformation()));//售后资料
            vo.setAfterSalesInformationPerson(userService.findByIds(it.getAfterSalesInformationPerson()));
            return vo;
        }).collect(Collectors.toList());
        return new Page<>(pageNo, pageSize, page.getTotal(), result);
    }

    /**
     * @param id
     * @return
     */
    @Override
    public boolean audit(Long id) {
        ProjectArchives projectArchives = projectArchivesDao.selectByPrimaryKey(id);
        projectArchives.setAudit(true);
        projectArchives.setAuditUser(UserTokenHolder.getUser().getId());
        projectArchives.setAuditTime(new Date());
        return projectArchivesDao.updateByPrimaryKeySelective(projectArchives)>0;
    }
}
