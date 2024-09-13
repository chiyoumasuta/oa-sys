package cn.gson.oasys.service.impl;

import cn.gson.oasys.dao.SysConfigDao;
import cn.gson.oasys.entity.config.SysConfig;
import cn.gson.oasys.service.SysConfigService;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;

@Service
public class SysConfigServiceImpl implements SysConfigService {

    @Resource
    private SysConfigDao sysConfigDao;

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
}
