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
        List<ReiType> reiTypeList = sysConfigService.getReiTypeList(null);

        Example reiExample = new Example(Reimbursement.class);
        Example reiItemExample = new Example(ReimbursementItem.class);
        return projectList.stream().map(p -> {
            ProjectCostStatisticsVo vo = new ProjectCostStatisticsVo();
            vo.setProjectName(p.getName() == null ? "日常开支" : p.getName());

            Example.Criteria criteria = reiExample.createCriteria();
            criteria.andEqualTo("project", p.getName() == null ? "日常开支" : p.getName());
            if (startDate != null) {
                criteria.andGreaterThan("startTime", startDate).andLessThan("entTime", endDate);
            }
            List<Reimbursement> reimbursements = reimbursementDao.selectByExample(reiExample).stream()
                    .filter(it->it.getStatus().equals(Reimbursement.Status.APPROVED))
                    .collect(Collectors.toList());
            reiExample.clear();
            if (reimbursements.isEmpty()) return vo;

            List<Long> ids = reimbursements.stream().map(Reimbursement::getId).collect(Collectors.toList());
            reiItemExample.createCriteria().andIn("reimbursementId", ids);
            List<ReimbursementItem> reimbursementItems = reimbursementItemDao.selectByExample(reiItemExample);
            reiItemExample.clear();
            if (reimbursements.isEmpty()) {
                return vo;
            }

            //总费用
            Double totalCoat = reimbursements.stream().mapToDouble(it-> it.getActualAmount()==null?it.getReimbursementAmount():it.getActualAmount()).sum();
            vo.setTotalCost(totalCoat);

            //项目实施费用统计
            if (!vo.getProjectName().equals("日常开支")) {
                List<Reimbursement> collect = reimbursements.stream().filter(it -> it.getType().equals(Reimbursement.ExpenseType.IMPLEMENTATION_FEE)).collect(Collectors.toList());
                if (!collect.isEmpty()) {
                    Double implementation = collect.stream().mapToDouble(Reimbursement::getReimbursementAmount).sum();
                    String participantsUser = reimbursementItems.stream().filter(it -> {
                        List<Long> id = collect.stream().map(Reimbursement::getId).collect(Collectors.toList());
                        return id.contains(it.getReimbursementId());
                    }).map(ReimbursementItem::getParticipants).distinct().collect(Collectors.joining(","));
                    vo.setImplementation(implementation);
                    vo.setImplementationName(participantsUser);
                } else {
                    vo.setImplementation(0.0);
                    vo.setImplementationName("");
                }
            }

            //差旅费分类统计/日常开支分类统计
            Map<String, Double> statistics = new HashMap<>();
            reiTypeList.forEach(t -> {
                List<ReimbursementItem> collect = reimbursementItems.stream().filter(it -> it.getType()!=null&&it.getType().equals(t.getName())).collect(Collectors.toList());
                if (!collect.isEmpty()) {
                    double sum = collect.stream().mapToDouble(ReimbursementItem::getCost).sum();
                    statistics.put(t.getName(), statistics.getOrDefault(t.getName(), 0.0) + sum);
                }
            });
            vo.setStatistics(statistics);

            //按报销人统计
            Map<String, Double> detailByUser = new HashMap<>();
            reimbursements.stream().filter(it -> !it.getType().equals(Reimbursement.ExpenseType.IMPLEMENTATION_FEE)).forEach(r -> {
                for (ReimbursementItem reimbursementItem : reimbursementItems.stream().filter(it -> it.getReimbursementId().equals(r.getId())).collect(Collectors.toList())) {
                    detailByUser.put(r.getSubmitUserName(), detailByUser.getOrDefault(r.getSubmitUserName(), 0.0) + reimbursementItem.getCost());
                }
            });
            vo.setDetailsByUser(detailByUser);

            //费用详情
            vo.setCostDetails(reimbursementItems);

            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 更具用户名返回费用统计结果
     *
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @param userId    用户id
     */
    @Override
    public Map<String, Map<String, Double>> countByUser(Date startDate, Date endDate, Long userId,String project) {
        //获取报销类型列表
        List<User> list = userService.page(null, null, 1, 9999).getList();
        Map<String, Map<String, Double>> result = new HashMap<>();

        List<Reimbursement> reimbursements = reimbursementDao.selectAll();
        if (project!=null){
            reimbursements = reimbursements.stream().filter(it->it.getProject().equals(project)&&it.getStatus().equals(Reimbursement.Status.APPROVED)).collect(Collectors.toList());
        }
        Example example = new Example(ReimbursementItem.class);

        List<Reimbursement> finalReimbursements = reimbursements;
        list.forEach(it -> {
            Map<String, Double> detail = new HashMap<>();
            List<Long> collect = finalReimbursements.stream().filter(r -> r.getSubmitUser().equals(it.getId())).map(Reimbursement::getId).collect(Collectors.toList());
            if (!collect.isEmpty()){
                example.createCriteria().andIn("reimbursementId", collect);
                List<ReimbursementItem> reimbursementItems = reimbursementItemDao.selectByExample(example);
                example.clear();
                if (!reimbursementItems.isEmpty()){
                    reimbursementItems = reimbursementItems.stream().filter(r -> r.getCost() != null).collect(Collectors.toList());
                    if (!reimbursementItems.isEmpty()) {
                        reimbursementItems.forEach(t -> detail.put(t.getType(), detail.getOrDefault(t.getType(), 0.0) + t.getCost()));
                        double totalCost = detail.values().stream().mapToDouble(Double::doubleValue).sum();
                        detail.put("总费用", totalCost);
                        result.put(it.getUserName(), detail);
                    }
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
