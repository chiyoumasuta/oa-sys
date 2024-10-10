package cn.gson.oasys.controller;

import cn.gson.oasys.entity.reimbursement.Reimbursement;
import cn.gson.oasys.entity.reimbursement.ReimbursementItem;
import cn.gson.oasys.service.ReimbursementService;
import cn.gson.oasys.support.UtilResultSet;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/reimbursement")
@Api(tags = "报销流程相关接口")
public class ReimbursementController {

    @Resource
    private ReimbursementService reimbursementService;

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
}
