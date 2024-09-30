package cn.gson.oasys.service.impl;

import cn.gson.oasys.dao.*;
import cn.gson.oasys.entity.User;
import cn.gson.oasys.entity.reimbursement.*;
import cn.gson.oasys.service.ReimbursementService;
import cn.gson.oasys.service.UserService;
import cn.gson.oasys.support.UserTokenHolder;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
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
        reimbursement.setStatus(Reimbursement.Status.SUBMITTED);
        reimbursement.setApprover(user.getId());
        reimbursement.setApproverName(user.getUserName());
        reimbursementDao.insert(reimbursement);

        dataKey=reimbursement.getId();

        List<ReimbursementItem> reimbursementItems = JSONArray.parseArray(dataList.get(1), ReimbursementItem.class);
        reimbursementItems.forEach(reimbursementItem -> {
            reimbursementItem.setReimbursementId(dataKey);
            reimbursementItemDao.insert(reimbursementItem);
        });

        Map<String,Object> variables = new HashMap<>();

//        variables.put("type", );
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


}