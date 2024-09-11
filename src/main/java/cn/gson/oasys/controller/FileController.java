package cn.gson.oasys.controller;

import cn.gson.oasys.entity.File;
import cn.gson.oasys.support.UtilResultSet;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import cn.gson.oasys.service.FileService;
import cn.gson.oasys.vo.FileListVo;

@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileService fileService;

    // 保存文件
    @PostMapping("/save")
    public UtilResultSet saveFile(MultipartFile file, Long nowPath, File.model model) {
        try {
            File result = fileService.saveFile(file, nowPath, model);
            return UtilResultSet.success(result);
        } catch (Exception e) {
            return UtilResultSet.bad_request("文件保存失败: " + e.getMessage());
        }
    }

    // 文件列表查看
    @GetMapping("/list")
    public UtilResultSet fileList(Long nowPath,String type,boolean inTrash) {
        try {
            FileListVo result = fileService.fileList(nowPath, type, inTrash);
            return UtilResultSet.success(result);
        } catch (Exception e) {
            return UtilResultSet.bad_request("文件列表加载失败: " + e.getMessage());
        }
    }

    // 删除文件到回收站
    @PostMapping("/drop")
    public UtilResultSet drop(Long fileId) {
        if (fileService.drop(fileId)) {
            return UtilResultSet.success("文件已移至回收站");
        } else {
            return UtilResultSet.bad_request("移动文件失败");
        }
    }

    // 彻底删除文件
    @DeleteMapping("/delete")
    public UtilResultSet delete(Long fileId) {
        if (fileService.delete(fileId)) {
            return UtilResultSet.success("文件彻底删除");
        } else {
            return UtilResultSet.bad_request("删除文件失败");
        }
    }

    // 重命名文件
    @PostMapping("/rename")
    public UtilResultSet rename(String fileId,String newName) {
        if (fileService.rename(fileId, newName)) {
            return UtilResultSet.success("文件重命名成功");
        } else {
            return UtilResultSet.bad_request("文件重命名失败");
        }
    }

    // 移动文件
    @PostMapping("/move")
    public UtilResultSet moveFile(String fileId,Long newFatherId) {
        if (fileService.moveFile(fileId, newFatherId)) {
            return UtilResultSet.success("文件移动成功");
        } else {
            return UtilResultSet.bad_request("文件移动失败");
        }
    }

//    // 审核文件
//    @PostMapping("/audit")
//    public UtilResultSet audit(Long fileId,String result,String sharePeople) {
//        try {
//            if (fileService.audit(fileId, result, sharePeople)) {
//                return UtilResultSet.success("审核成功");
//            } else {
//                return UtilResultSet.bad_request("审核失败");
//            }
//        } catch (ServiceException e) {
//            return UtilResultSet.bad_request("审核错误: " + e.getMessage());
//        }
//    }

    // 分享文件
    @PostMapping("/share")
    public UtilResultSet shareFile(String fileId,String sharePerson) {
        if (fileService.shareFile(fileId,sharePerson)) {
            return UtilResultSet.success("文件分享成功");
        } else {
            return UtilResultSet.bad_request("文件分享失败");
        }
    }
}
