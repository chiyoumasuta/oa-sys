package cn.gson.oasys.controller;

import cn.gson.oasys.entity.reimbursement.Reimbursement;
import cn.gson.oasys.entity.reimbursement.ReimbursementItem;
import cn.gson.oasys.service.ReimbursementService;
import cn.gson.oasys.support.UtilResultSet;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reimbursement")
@Api(tags = "报销流程相关接口")
public class ReimbursementController {

    @Resource
    private ReimbursementService reimbursementService;

    @RequestMapping(value = "/page",method = RequestMethod.POST)
    @ApiOperation(value = "分页查询 seachType:0:查询自己审核的流程 ")
    public UtilResultSet page(int pageSize, int pageNo, Date startDate, Date endDate, String project, int searchType,Reimbursement.Status status,Long person){
        return UtilResultSet.success(reimbursementService.page(pageSize,pageNo,startDate,endDate,project,searchType,status,person));
    }

    /**
     * 修改数据
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ApiOperation(value = "修改主数据")
    public UtilResultSet update(Reimbursement reimbursement) {
        reimbursementService.update(reimbursement);
        return UtilResultSet.success("更新成功");
    }

    /**
     * 修改明细表数据
     */
    @RequestMapping(value = "/updateItem", method = RequestMethod.POST)
    @ApiOperation(value = "修改明细表数据")
    public UtilResultSet updateItem(ReimbursementItem reimbursementItem) {
        reimbursementService.updateItem(reimbursementItem);
        return UtilResultSet.success("更新成功");
    }

    /**
     * 重新推送
     */
    @RequestMapping(value = "/reStart", method = RequestMethod.POST)
    @ApiOperation(value = "重新推送工单")
    public UtilResultSet reStart(String deployId, String dateJson, String type,Long oldId) {
        if (reimbursementService.reStart(deployId, dateJson, type,oldId)) {
            return UtilResultSet.success("重新发起成功");
        }
        return UtilResultSet.bad_request("重新发起失败");
    }



    @RequestMapping("/pdf")
    public void pdf(Long id, HttpServletResponse resp) throws IOException {
        Reimbursement reimbursement = reimbursementService.setNo(id);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

        resp.setContentType("application/pdf");
        // 编码nameById以处理中文字符
        String fileName = URLEncoder.encode(reimbursement.getSubmitUserName()+"--"+reimbursement.getProject()+"---"+sdf.format(reimbursement.getSubmitDate()), "UTF-8") + ".pdf";
        resp.setHeader("Content-Disposition", "attachment;fileName=" + fileName);
        ServletOutputStream os = resp.getOutputStream();
        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(os));

        pdfDoc.setDefaultPageSize(PageSize.A2);
        Document doc = new Document(pdfDoc);
        Document doc1 = reimbursementService.getDoc(doc,reimbursement);
        pdfDoc.close();
        doc1.close();
    }
}
