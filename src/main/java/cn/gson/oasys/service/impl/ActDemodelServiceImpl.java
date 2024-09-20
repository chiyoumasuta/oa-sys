package cn.gson.oasys.service.impl;

import cn.gson.oasys.dao.ActDeModelDao;
import cn.gson.oasys.entity.flowable.ActDeModel;
import cn.gson.oasys.service.ActDeModelService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@Service
public class ActDemodelServiceImpl implements ActDeModelService {

    @Resource
    private ActDeModelDao actDeModelDao;

    @Override
    public List<ActDeModel> getActDeModels() {
        return actDeModelDao.selectAll();
    }
}
