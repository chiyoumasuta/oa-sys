package cn.gson.oasys.service.impl;

import cn.gson.oasys.dao.LeaveApplicationDao;
import cn.gson.oasys.entity.LeaveApplication;
import cn.gson.oasys.service.LeaveApplicationService;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;

@Service
public class LeaveApplicationServiceImpl implements LeaveApplicationService {

    @Resource
    private LeaveApplicationDao leaveApplicationDao;

    /**
     * 审核接口
     *
     * @param id
     * @param result
     * @return
     */
    @Override
    public boolean audit(String id, String result) {
        LeaveApplication leaveApplication = leaveApplicationDao.selectByPrimaryKey(id);
        leaveApplication.setStats(result);
        boolean b = leaveApplicationDao.updateByPrimaryKey(leaveApplication) > 0;
        return b;
    }

    /**
     * 通过processInstanceId获取业务数据代码
     *
     * @param id
     */
    @Override
    public LeaveApplication getLeaveApplication(String id) {
        return leaveApplicationDao.selectByPrimaryKey(Long.valueOf(id));
    }
}
