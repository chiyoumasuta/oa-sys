package cn.gson.oasys.service.impl;

import cn.gson.oasys.dao.ActReprocdefDao;
import cn.gson.oasys.entity.ActReProcdef;
import cn.gson.oasys.service.ActReprocdefService;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ActReprocdefServiceImpl implements ActReprocdefService {

    @Resource
    private ActReprocdefDao actReprocdefDao;

    @Override
    public List<ActReProcdef> getActReprocdef() {
        List<ActReProcdef> result = new ArrayList<>();
        Map<String, List<ActReProcdef>> actReProcdefMap = actReprocdefDao.selectAll().stream().collect(Collectors.groupingBy(ActReProcdef::getKey));
        actReProcdefMap.forEach((k, v) -> {
            List<ActReProcdef> collect = v.stream()
                    .sorted(Comparator.comparing(ActReProcdef::getVersion).reversed())
                    .collect(Collectors.toList());
            result.add(collect.get(0));
        });
        return result;
    }

    @Override
    public boolean deleteByName(String name) {
        Example example = new Example(ActReProcdef.class);
        example.createCriteria().andEqualTo("key", name);
        return actReprocdefDao.deleteByExample(example) > 0;
    }
}
