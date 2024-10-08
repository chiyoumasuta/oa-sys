package cn.gson.oasys.controller;

import cn.gson.oasys.entity.FileAuditRecord;
import cn.gson.oasys.service.FileAuditRecordService;
import cn.gson.oasys.support.Page;
import cn.gson.oasys.support.UtilResultSet;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fileAudit")
@Api(tags = "上传文件审核")
public class FileAuditRecordController {

    @Autowired
    private FileAuditRecordService fileAuditRecordService;

    @RequestMapping(value = "/delete",method = RequestMethod.POST)
    @ApiOperation("取消审核")
    public UtilResultSet deleteFileAuditRecord(Long id) {
        boolean deleted = fileAuditRecordService.deleteFileAuditRecord(id);
        if (deleted) {
            return UtilResultSet.success("文件审核记录已删除");
        } else {
            return UtilResultSet.bad_request("删除文件审核记录失败");
        }
    }

    @RequestMapping(value = "/list",method = RequestMethod.POST)
    @ApiOperation("获取审核列表")
    @ApiImplicitParams({
        @ApiImplicitParam(name="pageNo",value="页码",required=true),
        @ApiImplicitParam(name="pageSize",value="页面大小",required=true),
        @ApiImplicitParam(name="searchType",value="搜索类型 1:我提交的 其他：需要我处理的",required=true)
    })
    public UtilResultSet findAllFileAuditRecords(@RequestParam int pageNo,@RequestParam int pageSize,@RequestParam int searchType) {
        Page<FileAuditRecord> page = fileAuditRecordService.findAllFileAuditRecords(pageNo, pageSize, searchType);
        return UtilResultSet.success(page);
    }

    @RequestMapping(value = "/audit",method = RequestMethod.POST)
    @ApiOperation("审核文件分享")
    public UtilResultSet auditFile(Long id, boolean result, String sharePeople) {
        boolean auditResult = fileAuditRecordService.audit(id, result, sharePeople);
        if (auditResult) {
            return UtilResultSet.success("文件审核成功");
        } else {
            return UtilResultSet.bad_request("文件审核失败");
        }
    }
}
