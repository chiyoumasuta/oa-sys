package cn.gson.oasys.service.impl;

import cn.gson.oasys.dao.ReimbursementDao;
import cn.gson.oasys.dao.ReimbursementItemDao;
import cn.gson.oasys.entity.Project;
import cn.gson.oasys.entity.User;
import cn.gson.oasys.entity.reimbursement.Reimbursement;
import cn.gson.oasys.entity.reimbursement.ReimbursementItem;
import cn.gson.oasys.service.ProjectCostStatisticsService;
import cn.gson.oasys.service.SysConfigService;
import cn.gson.oasys.service.UserService;
import cn.gson.oasys.vo.CostVo;
import cn.gson.oasys.vo.ProjectCostStatisticsVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectCostStatisticsServiceImpl implements ProjectCostStatisticsService {

    @Resource
    private SysConfigService sysConfigService;
    @Resource
    private ReimbursementDao reimbursementDao;
    @Resource
    private ReimbursementItemDao reimbursementItemDao;
    @Resource
    private UserService userService;

    @Override
    public List<ProjectCostStatisticsVo> countByProject(Date startDate, Date endDate, String project) {

        //项目列表
        List<Project> projectList = sysConfigService.getProjectList();

        //获取报销类型列表

        Example reiExample = new Example(Reimbursement.class);
        Example reiItemExample = new Example(ReimbursementItem.class);
        reiExample.createCriteria().andEqualTo("status", Reimbursement.Status.APPROVED);
        if (startDate != null) {
            Example.Criteria criteria = reiExample.createCriteria();
            criteria.andGreaterThan("startTime", startDate).andLessThan("entTime", startDate).andNotEqualTo("status", Reimbursement.Status.APPROVED);
            reiExample.and(criteria);
        }
        List<Reimbursement> reimbursements = reimbursementDao.selectByExample(reiExample);
        if (reimbursements.isEmpty())return new ArrayList<>();
        Map<Long,Reimbursement.ExpenseType> typeMap = reimbursements.stream().collect(Collectors.toMap(Reimbursement::getId,Reimbursement::getType));
        List<Long> ids = reimbursements.stream().map(Reimbursement::getId).collect(Collectors.toList());
        reiItemExample.createCriteria().andIn("reimbursementId", ids);
        Map<String, List<ReimbursementItem>> reimbursementItems = reimbursementItemDao.selectByExample(reiItemExample).stream().collect(Collectors.groupingBy(ReimbursementItem::getProject));

        List<ProjectCostStatisticsVo> result = new ArrayList<>();
        //子项目明细
        projectList.forEach(p -> {
            ProjectCostStatisticsVo father = new ProjectCostStatisticsVo();
            List<Project> pList = new ArrayList<>();
            if (p.getChildren() != null) {
                 pList = p.getChildren();
            }
            pList.add(p);
            List<ProjectCostStatisticsVo> childrenVoList = new ArrayList<>();
            pList.stream().filter(c->project==null||c.getName().equals(project)).forEach(c -> {
                ProjectCostStatisticsVo vo = new ProjectCostStatisticsVo();
                vo.setProjectName(c.getName());
                List<ReimbursementItem> detail = reimbursementItems.get(c.getName());
                //总费用
                double sum = 0.0;
                if (detail != null) {
                    sum = detail.stream().filter(it -> it.getCost() != null).mapToDouble(ReimbursementItem::getCost).sum();
                }
                double amortization = reimbursements.stream()
                        .filter(it -> it.getType().equals(Reimbursement.ExpenseType.AMORTIZATION) && it.getProject().equals(c.getName()))
                        .mapToDouble(Reimbursement::getReimbursementAmount).sum();
                final double[] implementationDays = {0.0};
                vo.setTotalCost(Math.round((sum+amortization) * 100.0) / 100.0);
                //项目实施费用统计
                double implementation = reimbursements.stream()
                        .filter(it -> it.getType().equals(Reimbursement.ExpenseType.IMPLEMENTATION_FEE) && it.getProject().equals(c.getName()))
                        .mapToDouble(it -> {
                            implementationDays[0] = implementationDays[0] + it.getDuration();
                            return it.getReimbursementAmount();
                        }).sum();
                vo.setImplementation(Math.round((implementation) * 100.0) / 100.0);
                //项目实施总天数
                vo.setImplementationDay(implementationDays[0]);
                //摊销费用
                father.setAmortization(Math.round((amortization) * 100.0) / 100.0);
                //项目参与人和天数明细
                Map<String, Double> implementationDayDetail = new HashMap<>();
                if (detail != null) {
                    detail.stream().filter(it -> it.getDays() != null).forEach(it -> {
                        implementationDayDetail.put(it.getParticipantsName(), implementationDayDetail.getOrDefault(it.getParticipantsName(), 0.0) + it.getDays());
                    });
                }
                vo.setImplementationDayDetail(implementationDayDetail);
                //实施费用分类统计
                Map<String, Double> implementationDetail = new HashMap<>();
                if (detail != null) {
                    detail.stream().filter(it -> typeMap.get(it.getReimbursementId()).equals(Reimbursement.ExpenseType.IMPLEMENTATION_FEE)&&it.getCost() != null).
                            forEach(it -> {
                                implementationDetail.put(it.getType(), Math.round((implementationDetail.getOrDefault(it.getType(), 0.0) + it.getCost()) * 100.0) / 100.0);
                    });
                }
                vo.setImplementationDetail(implementationDetail);
                //差旅费分类统计
                Map<String, Double> statistics = new HashMap<>();
                Map<String, Map<String, Double>> detailsByUser = new HashMap<>();
                if (detail != null) {
                    detail.stream().filter(it -> typeMap.get(it.getReimbursementId()).equals(Reimbursement.ExpenseType.TRAVEL_EXPENSES)&&it.getCost() != null).
                            forEach(it -> {
                                statistics.put(it.getType(), statistics.getOrDefault(it.getType(), 0.0) + it.getCost());
                                Map<String, Double> userDetail = detailsByUser.getOrDefault(it.getParticipantsName(), new HashMap<>());
                                userDetail.put(it.getType(), Math.round((userDetail.getOrDefault(it.getType(), 0.0) + it.getCost()) * 100.0) / 100.0);
                                detailsByUser.put(it.getParticipantsName(), userDetail);
                    });
                }
                vo.setStatistics(statistics);
                //按报销人明细
                vo.setDetailsByUser(detailsByUser);
                //费用明细
                vo.setCostDetails(detail);
                childrenVoList.add(vo);
            });

            father.setChildrenList(childrenVoList);
            //项目名称
            father.setProjectName(p.getName());
            //总费用
            father.setTotalCost(childrenVoList.stream().mapToDouble(ProjectCostStatisticsVo::getTotalCost).sum());
            //项目实施费用统计
            father.setImplementation(childrenVoList.stream().mapToDouble(ProjectCostStatisticsVo::getImplementation).sum());
            //项目实施总天数
            father.setImplementationDay(childrenVoList.stream().mapToDouble(ProjectCostStatisticsVo::getImplementationDay).sum());
            //摊销费用
            father.setAmortization(childrenVoList.stream().mapToDouble(ProjectCostStatisticsVo::getAmortization).sum());
            // 合并子项目的明细数据
            Map<String, Double> combinedImplementationDetail = new HashMap<>();
            childrenVoList.forEach(vo -> vo.getImplementationDayDetail().forEach((key, value) -> {
                combinedImplementationDetail.put(key, combinedImplementationDetail.getOrDefault(key, 0.0) + value);
            }));
            //项目参与人和参与天数明细
            father.setImplementationDayDetail(combinedImplementationDetail);
            Map<String, Double> combinedStatistics = new HashMap<>();
            childrenVoList.forEach(vo -> vo.getStatistics().forEach((key, value) -> {
                combinedStatistics.put(key, combinedStatistics.getOrDefault(key, 0.0) + value);
            }));
            //差旅费分类统计
            father.setStatistics(combinedStatistics);

            Map<String, Map<String, Double>> combinedDetailsByUser = new HashMap<>();
            childrenVoList.forEach(vo -> vo.getDetailsByUser().forEach((userKey, userValue) -> {
                Map<String, Double> userDetails = combinedDetailsByUser.getOrDefault(userKey, new HashMap<>());
                userValue.forEach((typeKey, typeValue) -> {
                    userDetails.put(typeKey, userDetails.getOrDefault(typeKey, 0.0) + typeValue);
                });
                combinedDetailsByUser.put(userKey, userDetails);
            }));
            //按报销人统计
            father.setDetailsByUser(combinedDetailsByUser);
            result.add(father);
        });
        return result;
    }

    /**
     * 更具用户名返回费用统计结果
     *
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @param userId    用户id
     */
    @Override
    public Map<String, Map<String, Double>> countByUser(Date startDate, Date endDate, Long userId, String project) {
        //获取报销类型列表
        List<User> list = userService.page(null, null, 1, 9999).getList();
        Map<String, Map<String, Double>> result = new HashMap<>();

        Example reiExample = new Example(Reimbursement.class);
        Example reiItemExample = new Example(ReimbursementItem.class);
        reiExample.createCriteria().andEqualTo("status", Reimbursement.Status.APPROVED);
        if (startDate != null) {
            Example.Criteria criteria = reiExample.createCriteria();
            criteria.andGreaterThan("startTime", startDate).andLessThan("entTime", endDate);
            reiExample.and(criteria);
        }
        List<Reimbursement> reimbursements = reimbursementDao.selectByExample(reiExample);
        List<Long> ids = reimbursements.stream().map(Reimbursement::getId).collect(Collectors.toList());
        reiItemExample.createCriteria().andIn("reimbursementId", ids);
        Map<String, List<ReimbursementItem>> reimbursementItems = reimbursementItemDao.selectByExample(reiItemExample).stream().filter(it->it.getParticipants()!=null).collect(Collectors.groupingBy(ReimbursementItem::getParticipants));

        list.forEach(it -> {
            List<ReimbursementItem> reimbursementItemsList = reimbursementItems.get(String.valueOf(it.getId()));
            Map<String, Double> detail = new HashMap<>();
            if (reimbursementItemsList != null) {
                reimbursementItemsList = reimbursementItemsList.stream().filter(r -> r.getCost() != null).collect(Collectors.toList());
                if (!reimbursementItemsList.isEmpty()) {
                    reimbursementItemsList.forEach(t -> detail.put(t.getType(), Math.round((detail.getOrDefault(t.getType(), 0.0) + t.getCost()) * 100.0) / 100.0));
                    double totalCost = Math.round((detail.values().stream().mapToDouble(Double::doubleValue).sum()) * 100.0) / 100.0;
                    detail.put("总费用", totalCost);
                    result.put(it.getUserName(), detail);
                }
            }
        });

        return result;
    }

    @Override
    public Map<String, Map<String, Double>> countByDept(Date startDate, Date endDate, String project) {
        Example reimbursementExample = new Example(Reimbursement.class);
        Example.Criteria criteria = reimbursementExample.createCriteria();
        criteria.andEqualTo("status", Reimbursement.Status.APPROVED).andNotEqualTo("type", Reimbursement.ExpenseType.IMPLEMENTATION_FEE);
        if (StringUtils.isNotBlank(project)) {
            criteria.andEqualTo("project", project);
        }
        if (startDate != null) {
            criteria.andGreaterThan("startTime", startDate).andLessThan("entTime", endDate);
        }
        List<Reimbursement> reimbursements = reimbursementDao.selectByExample(reimbursementExample);
        List<Long> collect = reimbursements.stream().map(Reimbursement::getId).collect(Collectors.toList());
        Example reimbursementItemExample = new Example(ReimbursementItem.class);
        reimbursementItemExample.createCriteria().andIn("reimbursementId", collect);

        Map<String, Map<String, Double>> resultMap = new HashMap<>();
        Map<String, Double> totalCostMap = new HashMap<>();

        reimbursementItemDao.selectByExample(reimbursementItemExample)
                .stream()
                .filter(it -> it.getCost() != null && it.getDept() != null)
                .forEach(it -> {
                    Map<String, Double> detail = resultMap.computeIfAbsent(it.getDept(), k -> new HashMap<>());
                    detail.put(it.getType(), Math.round((detail.getOrDefault(it.getType(), 0.0) + it.getCost()) * 100.0) / 100.0);
                    resultMap.put(it.getDept(), detail);
                    totalCostMap.put(it.getDept(), Math.round((totalCostMap.getOrDefault(it.getDept(), 0.0) + it.getCost()) * 100.0) / 100.0);
                });

        resultMap.forEach((key, value) -> value.put("总费用", totalCostMap.get(key)));

        return resultMap;
    }

    /**
     * 导出
     */
    @Override
    public List<CostVo> getCostVoList(String startDate, String endDate, String project, String type) {        //项目列表
        List<Project> projectList = sysConfigService.getProjectList();

        //获取报销类型列表

        Example reiExample = new Example(Reimbursement.class);
        Example reiItemExample = new Example(ReimbursementItem.class);
        reiExample.createCriteria().andEqualTo("status", Reimbursement.Status.APPROVED);
        if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            Example.Criteria criteria = reiExample.createCriteria();
            criteria.andBetween("startTime", startDate,endDate);
            reiExample.and(criteria);
        }
        List<Reimbursement> reimbursements = reimbursementDao.selectByExample(reiExample);
        if (reimbursements.isEmpty()){
            return Collections.emptyList();
        }
        Map<Long,Reimbursement.ExpenseType> typeMap = reimbursements.stream().collect(Collectors.toMap(Reimbursement::getId,Reimbursement::getType));
        List<Long> ids = reimbursements.stream().map(Reimbursement::getId).collect(Collectors.toList());
        reiItemExample.createCriteria().andIn("reimbursementId", ids);
        Map<String, List<ReimbursementItem>> reimbursementItems = reimbursementItemDao.selectByExample(reiItemExample).stream().collect(Collectors.groupingBy(ReimbursementItem::getProject));

        List<CostVo> result = new ArrayList<>();
        //子项目明细
        projectList.stream().filter(p -> !p.getChildren().isEmpty()).forEach(p -> {
            ProjectCostStatisticsVo father = new ProjectCostStatisticsVo();
            List<Project> childrenList = p.getChildren();
            childrenList.stream().filter(c->project==null|| project.isEmpty() ||c.getName().equals(project)).forEach(c -> {
                List<ReimbursementItem> detail = reimbursementItems.get(c.getName());
                if (detail != null) {
                    detail.stream().
                            filter(d->type==null||type.isEmpty()||Arrays.asList(type.split(",")).contains(typeMap.get(d.getReimbursementId()).name())).
                            forEach(d->{
                                CostVo costVo = new CostVo();
                                costVo.setProject(d.getProject()); //项目
                                costVo.setPerson(d.getParticipantsName()); //人员
                                costVo.setCostDetail(d.getType()); //费用明细
                                costVo.setCost(d.getCost()); //金额
                                costVo.setDays(d.getDays()); //天数
                                costVo.setRemark(d.getRemark()); //备注
                                result.add(costVo);
                            });
                }
            });
        });
        return result;
    }

}
