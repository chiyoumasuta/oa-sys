package cn.gson.oasys.service.impl;

import cn.gson.oasys.dao.ReimbursementDao;
import cn.gson.oasys.dao.ReimbursementItemDao;
import cn.gson.oasys.entity.Project;
import cn.gson.oasys.entity.ReiType;
import cn.gson.oasys.entity.User;
import cn.gson.oasys.entity.reimbursement.Reimbursement;
import cn.gson.oasys.entity.reimbursement.ReimbursementItem;
import cn.gson.oasys.service.ProjectCostStatisticsService;
import cn.gson.oasys.service.SysConfigService;
import cn.gson.oasys.service.UserService;
import cn.gson.oasys.vo.ProjectCostStatisticsVo;
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
    public List<ProjectCostStatisticsVo> countByProject(Date startDate, Date endDate, String project, String user) {

        //项目列表
        List<Project> projectList = sysConfigService.getProjectList();
        if (project != null) {
            projectList = projectList.stream().filter(p -> p.getName().equals(project)).collect(Collectors.toList());
        } else {
            projectList.add(new Project());
        }

        //获取报销类型列表

        Example reiExample = new Example(Reimbursement.class);
        Example reiItemExample = new Example(ReimbursementItem.class);
        reiExample.createCriteria().andEqualTo("status",Reimbursement.Status.APPROVED);
        if (startDate!=null){
            Example.Criteria criteria = reiExample.createCriteria();
            criteria.andGreaterThan("startTime", startDate).andLessThan("entTime", endDate).andNotEqualTo("status", Reimbursement.Status.APPROVED);
            reiExample.and(criteria);
        }
        List<Reimbursement> reimbursements = reimbursementDao.selectByExample(reiExample);
        List<Long> ids = reimbursements.stream().map(Reimbursement::getId).collect(Collectors.toList());
        reiItemExample.createCriteria().andIn("reimbursementId", ids);
        Map<String, List<ReimbursementItem>> reimbursementItems = reimbursementItemDao.selectByExample(reiItemExample).stream().collect(Collectors.groupingBy(ReimbursementItem::getProject));

        List<ProjectCostStatisticsVo> result = new ArrayList<>();
        projectList.stream().filter(p -> !p.getChildrenList().isEmpty()).forEach(p -> {
            ProjectCostStatisticsVo father = new ProjectCostStatisticsVo();
            List<Project> childrenList = p.getChildrenList();
            List<ProjectCostStatisticsVo> childrenVoList = new ArrayList<>();
            childrenList.forEach(c -> {
                ProjectCostStatisticsVo vo = new ProjectCostStatisticsVo();
                vo.setProjectName(p.getName());
                List<ReimbursementItem> detail = reimbursementItems.get(c.getName());
                //总费用
                double sum = detail.stream().filter(it -> it.getCost() != null).mapToDouble(ReimbursementItem::getCost).sum();
                final double[] implementation = {0.0};
                double sum1 = reimbursements.stream()
                        .filter(it -> it.getType().equals(Reimbursement.ExpenseType.IMPLEMENTATION_FEE) && it.getProject().equals(p.getName()))
                        .mapToDouble(it -> {
                            implementation[0] = implementation[0] + it.getDuration();
                            return it.getActualAmount();
                        }).sum();
                vo.setTotalCost(sum + sum1);
                //项目实施费用统计
                vo.setImplementation(sum1);
                //项目实施总天数
                vo.setImplementation(implementation[0]);
                //项目参与人和天数明细
                Map<String, Double> implementationDetail = new HashMap<>();
                detail.stream().filter(it -> it.getDays() != null).forEach(it -> {
                    implementationDetail.put(it.getProject(), implementationDetail.getOrDefault(it.getProject(), 0.0) + it.getCost());
                });
                vo.setImplementationDetail(implementationDetail);
                //差旅费分类统计
                Map<String, Double> statistics = new HashMap<>();
                Map<String, Map<String, Double>> detailsByUser = new HashMap<>();
                detail.stream().filter(it -> it.getCost() != null).forEach(it -> {
                    statistics.put(it.getType(), statistics.getOrDefault(it.getType(), 0.0) + it.getCost());
                    Map<String, Double> userDetail = detailsByUser.getOrDefault(it.getParticipants(), new HashMap<>());
                    userDetail.put(it.getType(), userDetail.getOrDefault(it.getType(), 0.0) + it.getCost());
                    detailsByUser.put(it.getParticipants(), userDetail);
                });
                vo.setStatistics(statistics);
                //按报销人明细
                vo.setDetailsByUser(detailsByUser);
                //费用明细
                vo.setCostDetails(detail.stream().filter(it -> it.getCost() != null).collect(Collectors.toList()));
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

            // 合并子项目的明细数据
            Map<String, Double> combinedImplementationDetail = new HashMap<>();
            childrenVoList.forEach(vo -> vo.getImplementationDetail().forEach((key, value) -> {
                combinedImplementationDetail.put(key, combinedImplementationDetail.getOrDefault(key, 0.0) + value);
            }));
            //项目参与人和参与天数明细
            father.setImplementationDetail(combinedImplementationDetail);

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
            // 设置父项目的详细费用
//            List<ReimbursementItem> combinedCostDetails = childrenVoList.stream()
//                    .flatMap(vo -> vo.getCostDetails().stream())
//                    .collect(Collectors.toList());
//            father.setCostDetails(combinedCostDetails);
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
        reiExample.createCriteria().andEqualTo("status",Reimbursement.Status.APPROVED);
        if (startDate!=null){
            Example.Criteria criteria = reiExample.createCriteria();
            criteria.andGreaterThan("startTime", startDate).andLessThan("entTime", endDate);
            reiExample.and(criteria);
        }
        List<Reimbursement> reimbursements = reimbursementDao.selectByExample(reiExample);
        List<Long> ids = reimbursements.stream().map(Reimbursement::getId).collect(Collectors.toList());
        reiItemExample.createCriteria().andIn("reimbursementId", ids);
        Map<String, List<ReimbursementItem>> reimbursementItems = reimbursementItemDao.selectByExample(reiItemExample).stream().collect(Collectors.groupingBy(ReimbursementItem::getParticipants));

        list.forEach(it -> {
            List<ReimbursementItem> reimbursementItemsList = reimbursementItems.get(it.getUserName());
            Map<String,Double> detail = new HashMap<>();
            if (!reimbursementItemsList.isEmpty()) {
                reimbursementItemsList = reimbursementItemsList.stream().filter(r -> r.getCost() != null).collect(Collectors.toList());
                if (!reimbursementItemsList.isEmpty()) {
                    reimbursementItemsList.forEach(t -> detail.put(t.getType(), detail.getOrDefault(t.getType(), 0.0) + t.getCost()));
                    double totalCost = detail.values().stream().mapToDouble(Double::doubleValue).sum();
                    detail.put("总费用", totalCost);
                    result.put(it.getUserName(), detail);
                }
            }
        });

        // 将结果按照总费用降序排序
        List<Map.Entry<String, Map<String, Double>>> sortedResult = result.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.comparingDouble(m -> -m.get("总费用")))).collect(Collectors.toList());

        // 将排序后的结果放入新的Map中
        Map<String, Map<String, Double>> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, Double>> entry : sortedResult) {
            sortedMap.put(entry.getKey(), result.get(entry.getKey()));
        }

        return sortedMap;
    }

}
