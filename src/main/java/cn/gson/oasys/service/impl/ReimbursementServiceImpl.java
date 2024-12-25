package cn.gson.oasys.service.impl;

import cn.gson.oasys.dao.*;
import cn.gson.oasys.entity.Department;
import cn.gson.oasys.entity.User;
import cn.gson.oasys.entity.reimbursement.*;
import cn.gson.oasys.service.*;
import cn.gson.oasys.support.exception.ServiceException;
import cn.gson.oasys.support.Page;
import cn.gson.oasys.support.UserTokenHolder;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReimbursementServiceImpl implements ReimbursementService {

    @Resource
    private ReimbursementDao reimbursementDao;
    @Resource
    private ReimbursementItemDao reimbursementItemDao;
    @Resource
    private RuntimeService runtimeService;
    @Resource
    private UserService userService;
    @Resource
    private DepartmentService departmentService;
    @Resource
    private FileService fileService;
    @Resource
    private SysConfigService sysConfigService;
    @Resource
    private UserDeptRoleService userDeptRoleService;

    @Override
    public Page<Reimbursement> page(int pageSize, int pageNo, Date startDate, Date endDate, String project, int searchType) {
        User user = UserTokenHolder.getUser();
        PageHelper.startPage(pageNo, pageSize);
        Example example = new Example(Reimbursement.class);
        Example.Criteria criteria = example.createCriteria();
        if (project != null) {
            criteria.andEqualTo("project", project);
        }
        if (startDate != null) {
            criteria.andEqualTo("startTime", startDate).andEqualTo("endTime", endDate);
        }
        if (searchType == 0&&!UserTokenHolder.isAdmin()) {
            criteria.andEqualTo("approver",user.getId());
        } else{
            criteria.andEqualTo("submitUser", user.getId());
        }
        criteria.andIn("status", Arrays.asList(Reimbursement.Status.APPROVED, Reimbursement.Status.REJECTED));
        com.github.pagehelper.Page<Reimbursement> data = (com.github.pagehelper.Page<Reimbursement>) reimbursementDao.selectByExample(example);
        List<Reimbursement> result = data.getResult().stream()
                .peek(it -> {
                    Example example1 = new Example(ReimbursementItem.class);
                    example1.createCriteria().andEqualTo("reimbursementId", it.getId());
                    it.setDetails(reimbursementItemDao.selectByExample(example1));
                })
                .sorted(Comparator.comparing(Reimbursement::getId))
                .collect(Collectors.toList());
        return new Page<>(pageNo, pageSize, data.getTotal(), result);
    }

    @Override
    @Transactional
    public boolean start(String deployId, String dataJson) {
        Long dataKey;
        User user = UserTokenHolder.getUser();
        user = userService.findById(user.getId());
        List<String> dataList = Arrays.asList(dataJson.split("&"));

        Reimbursement reimbursement = JSONObject.toJavaObject(JSONObject.parseObject(dataList.get(0)), Reimbursement.class);

        if (reimbursement.getProject()==null){
            reimbursement.setProject("其他");
        }

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

//        User userApprover = sysConfigService.getApproveByDept(reimbursement.getDepartmentName());

        //设置审核人
//        if (!userApprover.getUserName().equals(user.getUserName())) {
//            try {
//                reimbursement.setApprover(userApprover.getId());
//                reimbursement.setApproverName(userApprover.getUserName());
//                reimbursement.setStatus(Reimbursement.Status.REVIEW_1);
//            } catch (Exception e) {
//                throw new ServiceException("当前用户所在部门无负责人");
//            }
//        } else reimbursement.setStatus(Reimbursement.Status.ACCOUNTING);
        if (UserTokenHolder.getUser().getUserName().equals("阮咏薇")) {
            reimbursement.setStatus(Reimbursement.Status.GENERAL);
        }else if (userDeptRoleService.findByUserId(UserTokenHolder.getUser().getId()).stream().map(Department::getName).collect(Collectors.toList())
                .contains("综合管理中心")){
            if (UserTokenHolder.getUser().getUserName().equals("马涛")){
                reimbursement.setStatus(Reimbursement.Status.ACCOUNTING);
            }else {
                reimbursement.setStatus(Reimbursement.Status.REVIEW_2);
            }
        }else reimbursement.setStatus(Reimbursement.Status.REVIEW_1);

        //将附件从回收站移除
//        fileService.reDrop(reimbursement.getAttachmentId());
        reimbursementDao.insert(reimbursement);
        dataKey = reimbursement.getId();

        List<ReimbursementItem> reimbursementItems = JSONArray.parseArray(dataList.get(1), ReimbursementItem.class);
        final Double[] cost = {0.0};
        reimbursementItems.forEach(reimbursementItem -> {
            reimbursementItem.setReimbursementId(dataKey);
            reimbursementItem.setProject(reimbursementItem.getProject()==null?reimbursement.getProject():reimbursementItem.getProject());
            User byId = userService.findById(Long.valueOf(reimbursementItem.getParticipants()));
            if (byId==null) throw new ServiceException("参与人错误");
            reimbursementItem.setParticipantsName(byId.getUserName());
            reimbursementItemDao.insert(reimbursementItem);
            if (reimbursementItem.getCost() != null) {
                cost[0] = cost[0] + reimbursementItem.getCost();
            }
        });
        reimbursement.setReimbursementAmount(cost[0] == 0.0 ? reimbursement.getReimbursementAmount() : cost[0]);
        Map<String, Object> variables = new HashMap<>();
        //是否为主管（跳过主管审核流程）
//        if (!userApprover.getUserName().equals(user.getUserName())) {
//            variables.put("isManager", false);
//            //不是主管设置审核人
//            variables.put("manager", reimbursement.getApproverName());
//        } else variables.put("isManager", true);
        variables.put("type", reimbursement.getStatus().getLeave());
        reimbursementDao.updateByPrimaryKey(reimbursement);
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(deployId, String.valueOf(dataKey), variables);
        processInstance.getProcessInstanceId();
        return true;
    }

    @Override
    public Reimbursement getInfo(Long id, String searchType) {
        User user = UserTokenHolder.getUser();
        Reimbursement reimbursement = reimbursementDao.selectByPrimaryKey(id);
        Example exampleItem = new Example(ReimbursementItem.class);
        exampleItem.createCriteria().andEqualTo("reimbursementId", id);
        List<ReimbursementItem> reimbursementTravel = reimbursementItemDao.selectByExample(exampleItem);

        reimbursement.setDetails(reimbursementTravel);
        if (searchType != null && (
                (searchType.equals("1") && !reimbursement.getSubmitUser().equals(user.getId()))
                        || (searchType.equals("0") && (!Arrays.asList("阮咏薇", "熊蓉蓉").contains(user.getUserName()) || (reimbursement.getApprover() != null && !reimbursement.getApprover().equals(user.getId()))))
        )
        ) {
            return null;
        } else {
            return reimbursement;
        }
    }

    @Override
    public boolean audit(Long id, String result) {
        Reimbursement reimbursement = reimbursementDao.selectByPrimaryKey(id);
        Reimbursement.Status status = reimbursement.getStatus();
        switch (reimbursement.getStatus()) {
            case REVIEW_1:
                reimbursement.setApproverTime(new Date());
            case REVIEW_2:
                status = Reimbursement.Status.getNextStatus(status);
                break;
//            case MANAGER:
//                status = Reimbursement.Status.getNextStatus(status);
//                reimbursement.setApproverTime(new Date());
//                break;
            case ACCOUNTING:
                status = Reimbursement.Status.getNextStatus(status);
                if (!reimbursement.getType().equals(Reimbursement.ExpenseType.IMPLEMENTATION_FEE)) {
                    Example example = new Example(ReimbursementItem.class);
                    example.createCriteria().andEqualTo("reimbursementId", id);
                    final Double[] cost = {0.0};
                    List<ReimbursementItem> reimbursementItems = reimbursementItemDao.selectByExample(example);
                    if (!reimbursementItems.isEmpty()) {
                        reimbursementItems.forEach(reimbursementItem -> cost[0] = cost[0] + reimbursementItem.getCost());
                    }
                    reimbursement.setActualAmount(cost[0]==0.0?reimbursement.getReimbursementAmount():cost[0]);
                    reimbursement.setAccountingTime(new Date());
                } else {
                    reimbursement.setActualAmount(reimbursement.getReimbursementAmount());
                }
                break;
            case GENERAL:
                status = Reimbursement.Status.getNextStatus(status);
                reimbursement.setApprovalDate(new Date());
                break;
        }
        reimbursement.setStatus(status);
        return reimbursementDao.updateByPrimaryKeySelective(reimbursement) > 0;
    }

    @Override
    public void update(Reimbursement reimbursement) {
        User user = UserTokenHolder.getUser();
        if (!user.getUserName().equals("阮咏薇")) throw new ServiceException("只允许财务修改数据");
        Reimbursement oldData = reimbursementDao.selectByPrimaryKey(reimbursement.getId());
        if (oldData == null) {
            throw new ServiceException("未找到工单");
        }
        reimbursementDao.updateByPrimaryKeySelective(reimbursement);
    }

    @Override
    public void updateItem(ReimbursementItem reimbursementItem) {
        User user = UserTokenHolder.getUser();
        if (!user.getUserName().equals("阮咏薇")) throw new ServiceException("只允许财务修改数据");
        ReimbursementItem oldData = reimbursementItemDao.selectByPrimaryKey(reimbursementItem.getId());
        if (oldData == null) {
            throw new ServiceException("未找到数据");
        }
        reimbursementItemDao.updateByPrimaryKeySelective(reimbursementItem);
    }
}