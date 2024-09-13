package cn.gson.oasys.service.impl;

import cn.gson.oasys.dao.BusinessTravelDao;
import cn.gson.oasys.dao.OutputAndFollowDao;
import cn.gson.oasys.dao.ProjectProcessDao;
import cn.gson.oasys.entity.config.ProjectProcessConfig;
import cn.gson.oasys.entity.config.SysConfig;
import cn.gson.oasys.entity.User;
import cn.gson.oasys.entity.project.BusinessTravel;
import cn.gson.oasys.entity.project.OutputAndFollow;
import cn.gson.oasys.entity.project.ProjectProcess;
import cn.gson.oasys.service.ProjectProcessService;
import cn.gson.oasys.service.SysConfigService;
import cn.gson.oasys.service.UserService;
import cn.gson.oasys.support.Page;
import cn.gson.oasys.support.UserTokenHolder;
import cn.gson.oasys.vo.ProjectProcessVo;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang.StringUtils;
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
    private BusinessTravelDao businessTravelDao;
    @Resource
    private OutputAndFollowDao outputAndFollowDao;

    @Override
    public boolean createProject(ProjectProcess projectProcess) {
        User user = UserTokenHolder.getUser();
        projectProcess.setCreateUser(user.getUserName());
        projectProcess.setCreateTime(new Date());
        if (projectProcess.isNeedDevelopment()){
            //出差访问
            projectProcess.setStats(ProjectProcess.Stats.BUSINESS_TRAVEL);
            BusinessTravel businessTravel = new BusinessTravel();
            businessTravelDao.insert(businessTravel);
            projectProcess.setBusinessTravel(businessTravel.getId());
        }else {
            //方案输出
            projectProcess.setStats(ProjectProcess.Stats.SOLUTION_OUTPUT);
            OutputAndFollow outputAndFollow = new OutputAndFollow();
            outputAndFollowDao.insert(outputAndFollow);
            projectProcess.setOutputAndFollowUp(outputAndFollow.getId());
        }
        return projectProcessDao.insert(projectProcess)>0;
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
        }catch (Exception e){
            System.out.println(e.getMessage());
            throw new ServiceException("获取配置信息失败");
        }
    }
}
