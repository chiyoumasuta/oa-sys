package cn.gson.oasys.entity.config;

import cn.gson.oasys.entity.User;
import lombok.Data;

import java.util.List;

@Data
public class ProjectProcessConfig {
    //营销中心参与成员
    private List<Long> marketing;
    private List<User> marketingPerson;
    //研发中心参与成员
    private List<Long> rd;
    private List<User> rdPerson;
    //运营中心参与成员
    private List<Long> fulfillment;
    private List<User> fulfillmentPerson;
    //投标小组成员
    private List<Long> biddingPanel;
    private List<User> biddingPanelPerson;
    //综合管理中心参与成员
    private List<Long> integrated;
    private List<User> integratedPerson;
    //财务部
    private List<Long> department;
    private List<User> departmentPerson;
    //所有参与人
    private List<Long> allPerson;
}
