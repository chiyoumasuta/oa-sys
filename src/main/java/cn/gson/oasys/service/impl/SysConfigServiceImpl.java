package cn.gson.oasys.service.impl;

import cn.gson.oasys.dao.DepartmentDao;
import cn.gson.oasys.dao.SysConfigDao;
import cn.gson.oasys.entity.Department;
import cn.gson.oasys.entity.config.SysConfig;
import cn.gson.oasys.service.DepartmentService;
import cn.gson.oasys.service.SysConfigService;
import cn.gson.oasys.vo.SysConfigListVo;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SysConfigServiceImpl implements SysConfigService {

    @Resource
    private SysConfigDao sysConfigDao;
    @Resource
    private DepartmentDao departmentDao;

    @Override
    public boolean save(SysConfig sysConfig) {
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
        List<String> departments = departmentDao.selectAll().stream().filter(it->!it.isDeprecated()).map(Department::getName).collect(Collectors.toList());
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
            Arrays.stream(sysConfig.getValue().split(",")).filter(Objects::nonNull).forEach(it->{
                SysConfigListVo vo = new SysConfigListVo();
                vo.setName(it);
                result.add(vo);
            });
            return result;
        }else return Collections.emptyList();
    }

}