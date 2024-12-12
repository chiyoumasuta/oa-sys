package cn.gson.oasys.controller;

import cn.gson.oasys.dao.FileDao;
import cn.gson.oasys.entity.File;
import cn.gson.oasys.support.exception.ServiceException;
import cn.gson.oasys.support.UtilResultSet;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import cn.gson.oasys.service.FileService;
import cn.gson.oasys.vo.FileListVo;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/file")
@Api(tags = "文件管理")
public class FileController {

    @Value("${file.path}")
    private String rootPath;
    @Resource
    private FileService fileService;
    @Resource
    private FileDao fileDao;

    // 保存文件
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ApiOperation(value = "上传文件")
    public UtilResultSet saveFile(MultipartFile file, Long nowPath, File.model model, HttpServletRequest request) throws ServiceException {
        try {
            return UtilResultSet.success(fileService.saveFile(file, nowPath, model));
        } catch (Exception e) {
            return UtilResultSet.bad_request("文件保存失败: " + e.getMessage());
        }
    }

    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ApiOperation(value = "文件列表")
    public UtilResultSet fileList(Long nowPath, String type) {
        try {
            FileListVo result = fileService.fileList(nowPath, type);
            return UtilResultSet.success(result);
        } catch (Exception e) {
            return UtilResultSet.bad_request("文件列表加载失败: " + e.getMessage());
        }
    }

    @RequestMapping(value = "/reDrop", method = RequestMethod.POST)
    @ApiOperation(value = "回收站文件还原")
    public UtilResultSet reDrop(String fileIds) {
        if (fileService.reDrop(fileIds)) {
            return UtilResultSet.success("文件已移至回收站");
        } else {
            return UtilResultSet.bad_request("移动文件失败");
        }
    }

    /**
     * 下载文件
     *
     * @param response
     */
    @RequestMapping(value = "download", method = RequestMethod.GET)
    @ApiOperation(value = "下载文件")
    public void downFile(HttpServletResponse response, Long fileId) {
        try {
            File filelist = fileDao.selectByPrimaryKey(fileId);
            java.io.File file = fileService.getFile(filelist.getFilePath());
            response.setContentLength(filelist.getSize().intValue());
            response.setContentType(filelist.getContentType());
            response.setHeader("Content-Disposition", "attachment;filename=" + new String(filelist.getFileName().getBytes(StandardCharsets.UTF_8), "ISO8859-1"));
            writefile(response, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 写文件 方法
     */
    public void writefile(HttpServletResponse response, java.io.File file) {
        ServletOutputStream sos = null;
        FileInputStream aa = null;
        try {
            aa = new FileInputStream(file);
            sos = response.getOutputStream();
            // 读取文件问字节码
            byte[] data = new byte[(int) file.length()];
            IOUtils.readFully(aa, data);
            // 将文件流输出到浏览器
            IOUtils.write(data, sos);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                sos.close();
                aa.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 删除文件到回收站
    @RequestMapping(value = "/drop", method = RequestMethod.POST)
    @ApiOperation(value = "移动文件到回收站")
    public UtilResultSet drop(String fileId) {
        if (fileService.drop(fileId)) {
            return UtilResultSet.success("文件已移至回收站");
        } else {
            return UtilResultSet.bad_request("移动文件失败");
        }
    }


    @RequestMapping(value = "/makeFolder", method = RequestMethod.POST)
    @ApiOperation(value = "创建文件夹")
    public UtilResultSet makeFolder(Long nowPath, String name) {
        try {
            fileService.makeFolder(nowPath, name);
            return UtilResultSet.success("创建文件夹成功");
        } catch (Exception e) {
            return UtilResultSet.bad_request(e.getMessage());
        }
    }

    // 彻底删除文件
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ApiOperation(value = "彻底删除文件")
    public UtilResultSet delete(String fileId) {
        if (fileService.drop(fileId)) {
            return UtilResultSet.success("文件彻底删除");
        } else {
            return UtilResultSet.bad_request("删除文件失败");
        }
    }

    // 重命名文件
    @RequestMapping(value = "/rename", method = RequestMethod.POST)
    @ApiOperation(value = "重命名文件")
    public UtilResultSet rename(String fileId, String newName) {
        if (fileService.rename(fileId, newName)) {
            return UtilResultSet.success("文件重命名成功");
        } else {
            return UtilResultSet.bad_request("文件重命名失败");
        }
    }

    // 移动文件
    @RequestMapping(value = "/move", method = RequestMethod.POST)
    @ApiOperation(value = "移动文件")
    public UtilResultSet moveFile(String fileId, Long newFatherId) {
        if (fileService.moveFile(fileId, newFatherId)) {
            return UtilResultSet.success("文件移动成功");
        } else {
            return UtilResultSet.bad_request("文件移动失败");
        }
    }

    // 分享文件
    @RequestMapping(value = "/share", method = RequestMethod.POST)
    @ApiOperation(value = "分享文件")
    public UtilResultSet shareFile(String fileId, String sharePerson) {
        try {
            fileService.shareFile(fileId, sharePerson);
            return UtilResultSet.success("文件分享成功");
        } catch (Exception e) {
            return UtilResultSet.bad_request("文件分享失败:" + e.getMessage());
        }
    }

    //文件预览
    @RequestMapping(value = "/preview", method = RequestMethod.GET)
    @ApiOperation(value = "文件预览")
    public ResponseEntity<InputStreamResource> preview(Long fileId) throws IOException {
        File filelist = fileDao.selectByPrimaryKey(fileId);
        String path = this.rootPath + filelist.getFilePath();
        FileSystemResource file = new FileSystemResource(path);

        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", file.getFilename()));

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentLength(file.contentLength())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(new InputStreamResource(file.getInputStream()));
    }
}
