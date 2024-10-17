package cn.gson.oasys.service.impl;

import cn.gson.oasys.dao.*;
import cn.gson.oasys.entity.Department;
import cn.gson.oasys.entity.Project;
import cn.gson.oasys.entity.ReiType;
import cn.gson.oasys.entity.User;
import cn.gson.oasys.entity.config.SysConfig;
import cn.gson.oasys.entity.reimbursement.Reimbursement;
import cn.gson.oasys.entity.reimbursement.ReimbursementItem;
import cn.gson.oasys.support.JacksonUtil;
import cn.gson.oasys.support.exception.ServiceException;
import cn.gson.oasys.service.SysConfigService;
import cn.gson.oasys.vo.SysConfigListVo;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SysConfigServiceImpl implements SysConfigService {

    @Resource
    private SysConfigDao sysConfigDao;
    @Resource
    private DepartmentDao departmentDao;
    @Resource
    private ProjectDao projectDao;
    @Resource
    private ReiTypeDao reiTypeDao;
    @Resource
    private ReimbursementItemDao reimbursementItemDao;
    @Resource
    private ReimbursementDao reimbursementDao;
    @Resource
    private UserDao userDao;

    @Override
    public boolean saveOrUpdate(SysConfig sysConfig) {
        Example example = new Example(SysConfig.class);
        example.createCriteria().andEqualTo("name", sysConfig.getName());
        SysConfig sysConfig1 = sysConfigDao.selectOneByExample(example);
        if (sysConfig1 != null) {
            sysConfig1.setValue(sysConfig.getValue());
            return sysConfigDao.updateByPrimaryKeySelective(sysConfig1) > 0;
        }
        return sysConfigDao.insert(sysConfig) > 0;
    }

    @Override
    public SysConfig getSysConfig(String name) {
        Example example = new Example(SysConfig.class);
        example.createCriteria().andEqualTo("name", name);
        return sysConfigDao.selectOneByExample(example);
    }

    @Override
    public List<SysConfig> getLeaveConfig() {
        List<String> departments = departmentDao.selectAll().stream().filter(it -> !it.isDeprecated()).map(Department::getName).collect(Collectors.toList());
        Example example = new Example(SysConfig.class);
        example.createCriteria().andIn("name", departments);
        List<SysConfig> sysConfigs = sysConfigDao.selectByExample(example);
        return sysConfigs;
    }

    /**
     * @return 封装好的列表
     */
    @Override
    public List<SysConfigListVo> getSysConfigByList(String name) {
        List<SysConfigListVo> result = new ArrayList<>();
        SysConfig sysConfig = getSysConfig(name);
        if (sysConfig != null) {
            Arrays.stream(sysConfig.getValue().split(",")).filter(Objects::nonNull).forEach(it -> {
                SysConfigListVo vo = new SysConfigListVo();
                vo.setName(it);
                result.add(vo);
            });
            return result;
        } else return Collections.emptyList();
    }

    /**
     * 获取项目列表
     */
    @Override
    public List<Project> getProjectList() {
        return projectDao.selectAll();
    }

    @Override
    public boolean deleteProject(Long id) {
        return projectDao.deleteByPrimaryKey(id) > 0;
    }

    @Override
    @Transactional
    public boolean saveOrUpdate(Project project) {
        if (project.getId() == null) {
            Example example = new Example(Project.class);
            example.createCriteria().andEqualTo("name", project.getName());
            Project old = projectDao.selectOneByExample(example);
            if (old != null) {
                throw new ServiceException("项目名称重复");
            }
            return projectDao.insert(project) > 0;
        } else {
            Project old = projectDao.selectByPrimaryKey(project.getId());
            if (old == null) {
                throw new ServiceException("id错误");
            }
            Example reiExample = new Example(Reimbursement.class);
            reiExample.createCriteria().andEqualTo("project", old.getName());
            for (Reimbursement reimbursement : reimbursementDao.selectByExample(reiExample)) {
                reimbursement.setProject(project.getName());
                reimbursementDao.updateByPrimaryKeySelective(reimbursement);
            }
            return projectDao.updateByPrimaryKeySelective(project) > 0;
        }
    }

    @Override
    public List<ReiType> getReiTypeList(String type) {
        Example example = new Example(ReiType.class);
        if (type != null) {
            example.createCriteria().andEqualTo("type", type);
        }
        return reiTypeDao.selectByExample(example);
    }

    @Override
    public boolean deleteReiType(Long id) {
        return reiTypeDao.deleteByPrimaryKey(id) > 0;
    }

    @Override
    @Transactional
    public boolean saveOrUpdate(ReiType reiType) {
        if (reiType.getId() == null) {
            Example example = new Example(ReiType.class);
            example.createCriteria().andEqualTo("name", reiType.getName()).andEqualTo("type", reiType.getType());
            ReiType reiType1 = reiTypeDao.selectOneByExample(example);
            if (reiType1 != null) {
                throw new ServiceException("类型名称重复");
            }
            return reiTypeDao.insert(reiType) > 0;
        } else {
            ReiType old = reiTypeDao.selectByPrimaryKey(reiType.getId());
            if (old == null) {
                throw new ServiceException("id错误");
            }
            Example reiItemExample = new Example(ReimbursementItem.class);
            reiItemExample.createCriteria().andEqualTo("type", old.getName());
            for (ReimbursementItem reimbursementItem : reimbursementItemDao.selectByExample(reiItemExample)) {
                reimbursementItem.setType(reiType.getName());
                reimbursementItemDao.updateByPrimaryKeySelective(reimbursementItem);
            }
            return reiTypeDao.updateByPrimaryKeySelective(reiType) > 0;
        }
    }

    @Override
    public User getApproveByDept(String dept) {
        User userApprover;
        SysConfig sysConfig = getSysConfig(dept);
        try {
            JsonNode jsonNode = JacksonUtil.jsonStringToJsonNode(sysConfig.getValue());
            String approver = jsonNode.get("approver").asText();
            Example example = new Example(User.class);
            example.createCriteria().andEqualTo("userName", approver.split("%")[1]);
            userApprover = userDao.selectByExample(example).get(0);
        } catch (Exception e) {
            throw new ServiceException("获取审核人信息错误");
        }
        return userApprover;
    }

}