package cn.gson.oasys.service;

import cn.gson.oasys.entity.File;
import cn.gson.oasys.vo.FileListVo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {
    /**
     * 存储文件
     */
    File saveFile(MultipartFile file, Long nowPath, File.model model) throws IOException;

    /**
     * 获取当前文件夹文件列表
     */
    FileListVo fileList(Long nowPath, String type);

    /**
     * 将文件放入回收站
     */
    boolean drop(String fileId);

    /**
     * 文件从回收站还原
     */
    boolean reDrop(String fileId);

    /**
     * 将文件从回收站删除
     */
    boolean delete(String fileId);

    /**
     * 重命名文件/文件夹
     */
    boolean rename(String fileId,String newName);

    /**
     * 移动文件
     */
    boolean moveFile(String fileId,Long newFatherId);
    /**
     * 文件共享
     */
    public boolean shareFile(String fileId,String sharePerson);

    /**
     * 创建文件
     */
    boolean makeFolder(Long nowPath,String name);

    /**
     * 下载文件
     * @param filepath
     * @return
     */
    java.io.File getFile(String filepath);
}
