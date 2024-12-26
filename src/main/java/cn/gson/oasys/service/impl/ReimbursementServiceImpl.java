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
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.Style;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.layout.property.VerticalAlignment;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
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
        if (searchType == 0) {
            criteria.andLike("opinions","%"+user.getUserName()+"%");
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
            if (reimbursementItem.getParticipants() != null&& !reimbursementItem.getParticipants().isEmpty()) {
                User byId = userService.findById(Long.valueOf(reimbursementItem.getParticipants()));
                if (byId==null) throw new ServiceException("参与人错误");
                reimbursementItem.setParticipantsName(byId.getUserName());
            }
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
        if ("1".equals(searchType) && !reimbursement.getSubmitUser().equals(user.getId())
        ) {
            return null;
        } else {
            return reimbursement;
        }
    }

    @Override
    public boolean audit(Long id,boolean isPass, String result) {
        Reimbursement reimbursement = reimbursementDao.selectByPrimaryKey(id);
        Reimbursement.Status status = reimbursement.getStatus();
        reimbursement.setOpinions((reimbursement.getOpinions()==null?"":(reimbursement.getOpinions()+";"))+result);
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

    /**
     * @param id
     * @return
     */
    @Override
    public Reimbursement selectOneById(Long id) {
        User user = UserTokenHolder.getUser();
        Reimbursement data = reimbursementDao.selectByPrimaryKey(id);
        Example example1 = new Example(ReimbursementItem.class);
        example1.createCriteria().andEqualTo("reimbursementId", data.getId());
        data.setDetails(reimbursementItemDao.selectByExample(example1));
        return data;
    }

    @Override
    public Document getDoc(Document doc, Reimbursement data) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Style centeredStyle = new Style().setTextAlignment(TextAlignment.CENTER).setVerticalAlignment(VerticalAlignment.MIDDLE);
        String fontPath = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "template/font/simsun.ttf").getPath();
        PdfFont font = PdfFontFactory.createFont(fontPath, PdfEncodings.IDENTITY_H, false);

        float[] columnWidthsHeader = {100f, 300f, 100f, 700f};
        Table tableLog = new Table(columnWidthsHeader);
        //tableLog.setWidth(UnitValue.createPercentValue(100));
        //String[] titles= {"预勘名称","网络分类","预勘距离","经度","产品实例","归属分类","机房类型","纬度"};
        Cell head0 = new Cell(3, columnWidthsHeader.length).add(new Paragraph("报销单").addStyle(centeredStyle)).setFont(font).setBackgroundColor(new DeviceRgb(220, 220, 220));
        tableLog.addCell(head0);
        Cell h9 = new Cell(2, 1).add(new Paragraph("发起人").addStyle(centeredStyle)).setFont(font).setBackgroundColor(new DeviceRgb(220, 220, 220));
        Cell v9 = new Cell(2, 1).add(new Paragraph(data.getSubmitUserName())).setFont(font);
        Cell h10 = new Cell(2, 1).add(new Paragraph("发起时间").addStyle(centeredStyle)).setFont(font).setBackgroundColor(new DeviceRgb(220, 220, 220));
        Cell v10 = new Cell(2, 1).add(new Paragraph(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(data.getSubmitDate()))).setFont(font);
        Cell h0 = new Cell(2, 1).add(new Paragraph("费用类型").addStyle(centeredStyle)).setFont(font).setBackgroundColor(new DeviceRgb(220, 220, 220));
        Cell v0 = new Cell(2, 1).add(new Paragraph(data.getType().getName())).setFont(font);
        Cell h1 = new Cell(2, 1).add(new Paragraph("报销金额").addStyle(centeredStyle)).setFont(font).setBackgroundColor(new DeviceRgb(220, 220, 220));
        Cell v1 = new Cell(2, 1).add(new Paragraph(String.valueOf(data.getReimbursementAmount()))).setFont(font);
        Cell h2 = new Cell(2, 1).add(new Paragraph("地点").addStyle(centeredStyle)).setFont(font).setBackgroundColor(new DeviceRgb(220, 220, 220));
        Cell v2 = new Cell(2, 1).add(new Paragraph(data.getPlace())).setFont(font);
        Cell h3 = new Cell(2, 1).add(new Paragraph("所属项目").addStyle(centeredStyle)).setFont(font).setBackgroundColor(new DeviceRgb(220, 220, 220));
        Cell v3 = new Cell(2, 1).add(new Paragraph(data.getProject())).setFont(font);
        Cell h4 = new Cell(2, 1).add(new Paragraph("拜访单位").addStyle(centeredStyle)).setFont(font).setBackgroundColor(new DeviceRgb(220, 220, 220));
        Cell v4 = new Cell(2, 1).add(new Paragraph(data.getCompany())).setFont(font);
        Cell h5 = new Cell(2, 1).add(new Paragraph("关联工单").addStyle(centeredStyle)).setFont(font).setBackgroundColor(new DeviceRgb(220, 220, 220));
        Cell v5 = new Cell(2, 1).add(new Paragraph("无")).setFont(font);
        Cell h6 = new Cell(2, 1).add(new Paragraph("开始时间").addStyle(centeredStyle)).setFont(font).setBackgroundColor(new DeviceRgb(220, 220, 220));
        Cell v6 = new Cell(2, 1).add(new Paragraph(sdf.format(data.getStartTime())+data.getStartPeriod())).setFont(font);
        Cell h7 = new Cell(2, 1).add(new Paragraph("结束时间").addStyle(centeredStyle)).setFont(font).setBackgroundColor(new DeviceRgb(220, 220, 220));
        Cell v7 = new Cell(2, 1).add(new Paragraph(sdf.format(data.getEndTime())+data.getEndPeriod())).setFont(font);
        Cell h8 = new Cell(2, 1).add(new Paragraph("部门").addStyle(centeredStyle)).setFont(font).setBackgroundColor(new DeviceRgb(220, 220, 220));
        Cell v8 = new Cell(2, 1).add(new Paragraph(data.getDepartmentName())).setFont(font);
        tableLog.addCell(h9);
        tableLog.addCell(v9);
        tableLog.addCell(h10);
        tableLog.addCell(v10);
        tableLog.addCell(h0);
        tableLog.addCell(v0);
        tableLog.addCell(h1);
        tableLog.addCell(v1);
        tableLog.addCell(h2);
        tableLog.addCell(v2);
        tableLog.addCell(h3);
        tableLog.addCell(v3);
        tableLog.addCell(h4);
        tableLog.addCell(v4);
        tableLog.addCell(h5);
        tableLog.addCell(v5);
        tableLog.addCell(h6);
        tableLog.addCell(v6);
        tableLog.addCell(h7);
        tableLog.addCell(v7);
        tableLog.addCell(h8);
        tableLog.addCell(v8);
        doc.add(tableLog);

        float[] columnBoxWidths= {200f,100f,200f,200f,200f,300f};
        Table details = new Table(columnBoxWidths);
        details.setWidth(UnitValue.createPercentValue(100));
        Cell headB = new Cell(3, columnBoxWidths.length).add(new Paragraph("费用明细").addStyle(centeredStyle)).setFont(font).setBackgroundColor(new DeviceRgb(220, 220, 220));
        details.addHeaderCell(headB);
        Cell headerB1 = new Cell(2, 1).add(new Paragraph("参与人").addStyle(centeredStyle)).setFont(font).setBackgroundColor(new DeviceRgb(220, 220, 220));
        Cell headerB2 = new Cell(2, 1).add(new Paragraph("参与天数").addStyle(centeredStyle)).setFont(font).setBackgroundColor(new DeviceRgb(220, 220, 220));
        Cell headerB3 = new Cell(2, 1).add(new Paragraph("部门").addStyle(centeredStyle)).setFont(font).setBackgroundColor(new DeviceRgb(220, 220, 220));
        Cell headerB4 = new Cell(2, 1).add(new Paragraph("费用").addStyle(centeredStyle)).setFont(font).setBackgroundColor(new DeviceRgb(220, 220, 220));
        Cell headerB5 = new Cell(2, 1).add(new Paragraph("类型").addStyle(centeredStyle)).setFont(font).setBackgroundColor(new DeviceRgb(220, 220, 220));
        Cell headerB6 = new Cell(2, 1).add(new Paragraph("备注").addStyle(centeredStyle)).setFont(font).setBackgroundColor(new DeviceRgb(220, 220, 220));
        details.addHeaderCell(headerB1);
        details.addHeaderCell(headerB2);
        details.addHeaderCell(headerB3);
        details.addHeaderCell(headerB4);
        details.addHeaderCell(headerB5);
        details.addHeaderCell(headerB6);
        if (data.getDetails().isEmpty()) {
            Cell headE = new Cell(3, columnBoxWidths.length).add(new Paragraph("无详细数据").addStyle(centeredStyle)).setFont(font);
            details.addCell(headE);
        } else {
            for (ReimbursementItem r : data.getDetails()) {
                Cell cell1 = new Cell().add(new Paragraph(r.getParticipantsName())).setFont(font);
                Cell cell2 = new Cell().add(new Paragraph(String.valueOf(r.getDays()))).setFont(font);
                Cell cell3 = new Cell().add(new Paragraph(r.getDept())).setFont(font);
                Cell cell4 = new Cell().add(new Paragraph(String.valueOf(r.getCost()))).setFont(font);
                Cell cell5 = new Cell().add(new Paragraph(r.getType())).setFont(font);
                Cell cell6 = new Cell().add(new Paragraph(r.getRemark())).setFont(font);
                details.addCell(cell1);
                details.addCell(cell2);
                details.addCell(cell3);
                details.addCell(cell4);
                details.addCell(cell5);
                details.addCell(cell6);
            }
        }
        doc.add(details);

        float[] columnBoxWidths2= {400f,400f,400f};
        Table details2 = new Table(columnBoxWidths2);
        details2.setWidth(UnitValue.createPercentValue(100));
        Cell headC = new Cell(3, columnBoxWidths.length).add(new Paragraph("审核明细").addStyle(centeredStyle)).setFont(font).setBackgroundColor(new DeviceRgb(220, 220, 220));
        details2.addHeaderCell(headC);
        Cell headerC1 = new Cell(2, 1).add(new Paragraph("审核人").addStyle(centeredStyle)).setFont(font).setBackgroundColor(new DeviceRgb(220, 220, 220));
        Cell headerC2 = new Cell(2, 1).add(new Paragraph("审核意见").addStyle(centeredStyle)).setFont(font).setBackgroundColor(new DeviceRgb(220, 220, 220));
        Cell headerC3 = new Cell(2, 1).add(new Paragraph("审核结果").addStyle(centeredStyle)).setFont(font).setBackgroundColor(new DeviceRgb(220, 220, 220));
        details2.addHeaderCell(headerC1);
        details2.addHeaderCell(headerC2);
        details2.addHeaderCell(headerC3);
        if (data.getOpinionsList().isEmpty()) {
            Cell headE = new Cell(3, columnBoxWidths.length).add(new Paragraph("无详细数据").addStyle(centeredStyle)).setFont(font);
            details2.addCell(headE);
        } else {
            for (String r : data.getOpinionsList()) {
                Cell cell1 = new Cell().add(new Paragraph(r.split("@@")[0])).setFont(font);
                Cell cell2 = new Cell().add(new Paragraph(r.split("@@")[1])).setFont(font);
                Cell cell3 = new Cell().add(new Paragraph(r.split("@@")[2])).setFont(font);
                details2.addCell(cell1);
                details2.addCell(cell2);
                details2.addCell(cell3);
            }
        }
        doc.add(details2);
        doc.close();
        return doc;
    }
}