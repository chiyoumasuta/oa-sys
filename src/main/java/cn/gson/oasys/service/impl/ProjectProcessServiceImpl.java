package cn.gson.oasys.service.impl;

import cn.gson.oasys.dao.ProjectProcessDao;
import cn.gson.oasys.entity.config.ProjectProcessConfig;
import cn.gson.oasys.entity.config.SysConfig;
import cn.gson.oasys.entity.User;
import cn.gson.oasys.entity.ProjectProcess;
import cn.gson.oasys.flowable.utils.FlowableApiUtils;
import cn.gson.oasys.service.ProjectProcessService;
import cn.gson.oasys.service.SysConfigService;
import cn.gson.oasys.service.UserService;
import cn.gson.oasys.support.Page;
import cn.gson.oasys.support.UserTokenHolder;
import cn.gson.oasys.vo.ProjectProcessVo;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang.StringUtils;
import org.flowable.engine.*;
import org.flowable.engine.runtime.ProcessInstance;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectProcessServiceImpl implements ProjectProcessService {

    @Resource
    private ProjectProcessDao projectProcessDao;
    @Resource
    private SysConfigService sysConfigService;
    @Resource
    private UserService userService;
    @Resource
    private IdentityService identityService;
    @Resource
    private RuntimeService runtimeService;
    @Resource
    private FlowableApiUtils flowableApiUtils;

    @Override
    public boolean createProject(ProjectProcess projectProcess) {
        User user = UserTokenHolder.getUser();
        List<org.flowable.idm.api.User> users = identityService.createUserQuery().list();
        org.flowable.idm.api.User user1 = users.stream().filter(it -> it.getDisplayName().equals(user.getUserName())).collect(Collectors.toList()).get(0);
        projectProcess.setCreateUser(user.getUserName());
        projectProcess.setCreateTime(new Date());
        if (projectProcessDao.insert(projectProcess)>0){
            // 设置发起人
            identityService.setAuthenticatedUserId(user1.getId());
            // 根据流程 ID 启动流程

            Map<String,Object> variables = new HashMap<>();
            // 设置assignee的取值
            variables.put("assignee0","张三");
            variables.put("assignee1","李四");
            variables.put("assignee2","王五");
            variables.put("assignee3","赵财务");
            // 启动流程实例，第一个参数是流程定义的id
            ProcessInstance processInstance = runtimeService.startProcessInstanceById("22cedfd0-7593-11ef-8029-c29ca75c1290", String.valueOf(projectProcess.getId()),variables);
            // 输出相关的流程实例信息
            System.out.println("流程定义的ID：" + processInstance.getProcessDefinitionId());
            System.out.println("流程实例的ID：" + processInstance.getId());
            System.out.println("当前活动的ID：" + processInstance.getActivityId());
            return true;
        }
        return false;
    }

    @Override
    public Page<ProjectProcess> page(int pageNo, int pageSize, String name) {
        PageHelper.startPage(pageNo, pageSize);
        Example example = new Example(ProjectProcess.class);
        if (StringUtils.isNotBlank(name)) {
            example.createCriteria().andLike("name", "%" + name + "%");
        }
        com.github.pagehelper.Page<ProjectProcess> pageInfo = (com.github.pagehelper.Page) projectProcessDao.selectByExample(example);
        return new Page<>(pageNo,pageSize,pageInfo.getTotal(),pageInfo.getResult());
    }

    @Override
    public ProjectProcessVo getInfo(Long id) {
        ProjectProcessVo result = new ProjectProcessVo();
        ProjectProcess projectProcess = projectProcessDao.selectByPrimaryKey(id);
        BeanUtils.copyProperties(projectProcess, result);
        //zb TODO 获取流程详细信息 汇总后的详细信息
        return result;
    }

    @Override
    public ProjectProcessConfig getConfig() {
        try {
            ProjectProcessConfig config = new ProjectProcessConfig();
            SysConfig sysConfig = sysConfigService.getSysConfig("ProjectProcessConfig");
            String jsonStr = sysConfig.getValue();
            JSONObject jsonObject = JSONObject.parseObject(jsonStr);
            config.setMarketing(Arrays.stream(jsonObject.getString("marketing").split(",")).filter(Objects::nonNull).map(Long::valueOf).collect(Collectors.toList()));
            config.setMarketingPerson(userService.findDetailByIds(config.getMarketing()));
            List<Long> allPerson = new ArrayList<>(config.getMarketing());
            config.setRd(Arrays.stream(jsonObject.getString("rd").split(",")).filter(Objects::nonNull).map(Long::valueOf).collect(Collectors.toList()));
            config.setRdPerson(userService.findDetailByIds(config.getRd()));
            allPerson.addAll(config.getRd());
            config.setFulfillment(Arrays.stream(jsonObject.getString("fulfillment").split(",")).filter(Objects::nonNull).map(Long::valueOf).collect(Collectors.toList()));
            config.setFulfillmentPerson(userService.findDetailByIds(config.getFulfillment()));
            allPerson.addAll(config.getFulfillment());
            config.setBiddingPanel(Arrays.stream(jsonObject.getString("biddingPanel").split(",")).filter(Objects::nonNull).map(Long::valueOf).collect(Collectors.toList()));
            config.setBiddingPanelPerson(userService.findDetailByIds(config.getBiddingPanel()));
            allPerson.addAll(config.getBiddingPanel());
            config.setIntegrated(Arrays.stream(jsonObject.getString("integrated").split(",")).filter(Objects::nonNull).map(Long::valueOf).collect(Collectors.toList()));
            config.setIntegratedPerson(userService.findDetailByIds(config.getIntegrated()));
            allPerson.addAll(config.getIntegrated());
            config.setDepartment(Arrays.stream(jsonObject.getString("department").split(",")).filter(Objects::nonNull).map(Long::valueOf).collect(Collectors.toList()));
            config.setDepartmentPerson(userService.findDetailByIds(config.getDepartment()));
            allPerson.addAll(config.getDepartment());
            config.setAllPerson(allPerson.stream().distinct().collect(Collectors.toList()));
            return config;
        }catch (Exception e) {
            System.out.println(e.getMessage());
            throw new ServiceException("获取配置信息失败");
        }
    }

    /**
     * 审核
     * @param taskId 任务节点 Id
     * @param projectProcess 业务数据
     * @param nextReviewer 下一个审核人节点，审核人参数
     * @param fileId 上传文件id
     * @param presentation 报告
     * @return
     */
    public Boolean taskByAssignee(String taskId,ProjectProcess projectProcess,String nextReviewer,Long fileId,String presentation) {
        User user = UserTokenHolder.getUser();
        //获取旧的业务数据
        ProjectProcess oldBusinessData = projectProcessDao.selectByPrimaryKey(projectProcess.getId());

        Map<String, Object> map = new HashMap<>();
        // 设置下一个节点的参数
        map.put("reviewer", nextReviewer);

        //判断当前流程状态做出相应 处理
        switch (oldBusinessData.getStats()){
            case FOLLOW_UP:
                map.put("pass",oldBusinessData.isPass());
                break;
            case WON_BID:
                map.put("development",oldBusinessData.isNeedDevelopment());
                break;
            case APPROVED:
                map.put("approved",oldBusinessData.isApproved());
                break;
        }

        flowableApiUtils.setVariables(taskId,map);

        // 根据任务节点 Id，获取流程实例 Id
        String processInstanceId = flowableApiUtils.getTaskInfo(taskId);

        // 完成任务，taskId 任务节点 ID
        flowableApiUtils.taskByAssignee(taskId, user.getUserName(), map);
        projectProcess.setStats(ProjectProcess.Stats.getNextStats(projectProcess.getStats()));

        // 通过流程实例 Id，判断流程是否结束
        boolean isFinish = flowableApiUtils.checkProcessInstanceFinish(processInstanceId);
        if (isFinish) {
            projectProcess.setStats(ProjectProcess.Stats.DONE);
        }
        projectProcessDao.updateByPrimaryKeySelective(projectProcess);
        return true;
    }
}
