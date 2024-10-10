package cn.gson.oasys.service.impl;

import cn.gson.oasys.dao.*;
import cn.gson.oasys.entity.Department;
import cn.gson.oasys.entity.LeaveApplication;
import cn.gson.oasys.entity.User;
import cn.gson.oasys.entity.reimbursement.*;
import cn.gson.oasys.exception.ServiceException;
import cn.gson.oasys.service.DepartmentService;
import cn.gson.oasys.service.ReimbursementService;
import cn.gson.oasys.service.UserService;
import cn.gson.oasys.support.Page;
import cn.gson.oasys.support.UserTokenHolder;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import javax.sql.rowset.serial.SerialException;
import java.util.*;

@Service
public class ReimbursementServiceImpl implements ReimbursementService {

    @Resource
    private ReimbursementDao reimbursementDao; //工单
    @Resource
    private ReimbursementItemDao reimbursementItemDao;
    @Resource
    private RuntimeService runtimeService;
    @Resource
    private UserService userService;
    @Resource
    private DepartmentService departmentService;

    /**
     * 获取报销数据列表
     *
     * @param pageSize  分页大小
     * @param pageNo    页号
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @param project   项目名称
     */
    @Override
    public Page<Reimbursement> getList(int pageSize, int pageNo, Date startDate, Date endDate, String project) {
        PageHelper.startPage(pageNo, pageSize);
        Example example = new Example(Reimbursement.class);
        Example.Criteria criteria = example.createCriteria();
        if (project != null) {
            criteria.andEqualTo("project", project);
        }
        if (startDate != null) {
            criteria.andEqualTo("startTime", startDate).andEqualTo("endTime", endDate);
        }
        com.github.pagehelper.Page<Reimbursement> data = (com.github.pagehelper.Page<Reimbursement>) reimbursementDao.selectByExample(example);
        return new Page<>(pageNo, pageSize, data.getTotal(), data.getResult());
    }

    /**
     * 实例化项目
     *
     * @param deployId
     * @param dataJson
     */
    @Override
    public boolean start(String deployId, String dataJson) {
        Long dataKey;
        User user = UserTokenHolder.getUser();
        List<String> dataList = Arrays.asList(dataJson.split("&"));
        Reimbursement reimbursement = JSONObject.toJavaObject(JSONObject.parseObject(dataList.get(0)), Reimbursement.class);

        //设置提交人
        reimbursement.setSubmitUser(user.getId());
        reimbursement.setSubmitUserName(user.getUserName());
        reimbursement.setSubmitDate(new Date());

        //设置审核人和部门
        List<Department> departmentById = departmentService.findDepartmentById(String.valueOf(reimbursement.getDepartment()));
        Department department;
        if (!departmentById.isEmpty()) {
            department = departmentById.get(0);
        } else throw new ServiceException("未找到部门");
        reimbursement.setDepartmentName(department.getName());

        //设置审核人
        if (!user.isManager()) {
            User manager = userService.findById(department.getManagerId());
            reimbursement.setApprover(manager.getId());
            reimbursement.setApproverName(manager.getUserName());
            reimbursement.setStatus(Reimbursement.Status.MANAGER);
        } else reimbursement.setStatus(Reimbursement.Status.ACCOUNTING);

        reimbursementDao.insert(reimbursement);

        dataKey = reimbursement.getId();

        List<ReimbursementItem> reimbursementItems = JSONArray.parseArray(dataList.get(1), ReimbursementItem.class);
        reimbursementItems.forEach(reimbursementItem -> {
            reimbursementItem.setReimbursementId(dataKey);
            reimbursementItemDao.insert(reimbursementItem);
        });
        Map<String, Object> variables = new HashMap<>();
        //是否为主管（跳过主管审核流程）
        variables.put("isManager", user.isManager());
        //不是主管设置审核人
        variables.put("manager", userService.findById(department.getManagerId()).getUserName());
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(deployId, String.valueOf(dataKey), variables);
        processInstance.getProcessInstanceId();
        return true;
    }

    /**
     * 获取业务代码信息
     *
     * @param id
     */
    @Override
    public Reimbursement getInfo(Long id) {
        Reimbursement reimbursement = reimbursementDao.selectByPrimaryKey(id);
        Example exampleItem = new Example(ReimbursementItem.class);
        exampleItem.createCriteria().andEqualTo("reimbursementId", id);
        List<ReimbursementItem> reimbursementTravel = reimbursementItemDao.selectByExample(exampleItem);
        reimbursement.setDetails(reimbursementTravel);
        return reimbursement;
    }

    /**
     * 审核接口
     */
    @Override
    public boolean audit(String id, String result) {
        Reimbursement reimbursement = reimbursementDao.selectByPrimaryKey(id);
        Reimbursement.Status status = reimbursement.getStatus();
        switch (reimbursement.getStatus()) {
            case MANAGER:
                status = Reimbursement.Status.getNextStatus(status);
                reimbursement.setApproverTime(new Date());
                break;
            case ACCOUNTING:
                status = Reimbursement.Status.getNextStatus(status);
                if (result == null) throw new ServiceException("请确认实际报销金额");
                reimbursement.setActualAmount(Double.valueOf(result));
                reimbursement.setAccountingTime(new Date());
                break;
            case GENERAL:
                status = Reimbursement.Status.getNextStatus(status);
                reimbursement.setApprovalDate(new Date());
                break;
        }
        reimbursement.setStatus(status);
        return false;
    }

    /**
     * 修改数据
     *
     * @param reimbursement
     */
    @Override
    public void update(Reimbursement reimbursement) {
        User user = UserTokenHolder.getUser();
        if (!user.getUserName().equals("阮永薇")) throw new ServiceException("只允许财务修改数据");
        Reimbursement oldData = reimbursementDao.selectByPrimaryKey(reimbursement.getId());
        if (oldData == null) {
            throw new ServiceException("未找到工单");
        }
        reimbursementDao.updateByPrimaryKeySelective(reimbursement);
    }

    /**
     * 修改明细表数据
     *
     * @param reimbursementItem
     */
    @Override
    public void updateItem(ReimbursementItem reimbursementItem) {
        User user = UserTokenHolder.getUser();
        if (!user.getUserName().equals("阮永薇")) throw new ServiceException("只允许财务修改数据");
        ReimbursementItem oldData = reimbursementItemDao.selectByPrimaryKey(reimbursementItem.getId());
        if (oldData == null) {
            throw new ServiceException("未找到数据");
        }
        reimbursementItemDao.updateByPrimaryKeySelective(reimbursementItem);
    }
}