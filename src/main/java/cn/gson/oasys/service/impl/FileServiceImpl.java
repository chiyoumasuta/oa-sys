package cn.gson.oasys.service.impl;

import cn.gson.oasys.dao.FileDao;
import cn.gson.oasys.entity.Department;
import cn.gson.oasys.entity.File;
import cn.gson.oasys.entity.FileAuditRecord;
import cn.gson.oasys.entity.User;
import cn.gson.oasys.service.DepartmentService;
import cn.gson.oasys.service.FileAuditRecordService;
import cn.gson.oasys.service.FileService;
import cn.gson.oasys.service.UserService;
import cn.gson.oasys.support.UserTokenHolder;
import cn.gson.oasys.vo.FileListVo;
import org.apache.commons.io.FilenameUtils;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.io.*;
import java.rmi.ServerException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FileServiceImpl implements FileService {

    @Value("${file.path}")
    private String rootPath;
    @Resource
    private FileDao flDao;
    @Resource
    private FileAuditRecordService fileAuditRecordService;
    @Resource
    private DepartmentService departmentService;
    @Resource
    private UserService userService;

    @Override
    public Long saveFile(MultipartFile file, Long nowPath, File.model model) throws IllegalStateException, IOException {
        User user = UserTokenHolder.getUser();
        if (user==null){
            throw new ServerException("用户信息获取失败");
        }
        java.io.File savepath = new java.io.File(this.rootPath,user.getUserName());
        if (!savepath.exists()) {
            savepath.mkdirs();
        }

        String type = FilenameUtils.getExtension(file.getOriginalFilename());
        String newFileName = UUID.randomUUID().toString().toLowerCase()+"."+type;
        java.io.File targetFile = new java.io.File(savepath,newFileName);
        file.transferTo(targetFile);

        File filelist = new File();
        String filename = file.getOriginalFilename();
        filelist.setFileName(filename);
        filelist.setFilePath(targetFile.getAbsolutePath().replace("\\", "/").replace(this.rootPath, ""));
        filelist.setType(type);
        filelist.setModel(model==null?File.model.CLOUD:model);
        filelist.setFather(nowPath==null?0:nowPath);
        filelist.setSize(file.getSize());
        filelist.setUploadTime(new Date());
        filelist.setContentType(file.getContentType());
        filelist.setUserId(user.getId());
        flDao.insert(filelist);

        return filelist.getFileId();
    }

    @Override
    public boolean makeFolder(Long nowPath,String name){
        User user = UserTokenHolder.getUser();
        java.io.File savepath = new java.io.File(this.rootPath,user.getUserName());
        java.io.File folder = new java.io.File(savepath,name);
        if (!folder.exists()){
            if (!folder.mkdirs()){
                return false;
            }
        }
        File filelist = new File();
        String filename = name;
        filelist.setFileName(filename);
        filelist.setFilePath(folder.getAbsolutePath().replace("\\", "/").replace(this.rootPath, ""));
        filelist.setType("folder");
        filelist.setFather(nowPath==null?0:nowPath);
        filelist.setUploadTime(new Date());
        filelist.setUserId(user.getId());
        flDao.insert(filelist);
        return true;
    }

    @Override
    public FileListVo fileList(Long nowPath, String type) {
        Long userId = UserTokenHolder.getUser().getId();
        Long father = nowPath==null?0:nowPath;
        Example example = new Example(File.class);
        if ("回收站".equals(type)||"共享文件夹".equals(type)){
            example.createCriteria().andLike("sharePeople","%"+userId+"%").orEqualTo("userId",userId);
        }else {
            example.createCriteria().andLike("sharePeople","%"+userId+"%").orEqualTo("userId",userId).andEqualTo("father",father);
        }
        List<File> byUserIdAndFather = flDao.selectByExample(example);
        FileListVo result = new FileListVo();
        type = type==null?"所有文件":type;
        switch (type){
            case "所有文件":
                result.setFile(byUserIdAndFather.stream()
                        .filter(it->!it.isFileInTrash()&&it.getUserId().equals(userId))
                        .collect(Collectors.toList()));
                break;
            case "回收站":
                result.setFile(byUserIdAndFather.stream()
                        .filter(it-> it.isFileInTrash()&&Objects.equals(it.getUserId(), userId))
                        .collect(Collectors.toList()));
                break;
            case "报销附件":
                result.setFile(byUserIdAndFather.stream()
                        .filter(it->it.getModel().equals(File.model.REIMBURSEMENT)&&it.getUserId().equals(userId))
                        .collect(Collectors.toList()));
                break;
            case "待审核":
                result.setFile(byUserIdAndFather.stream()
                        .filter(it->(it.getStatus()==1||it.getStatus()==2||it.getStatus()==3)&&it.getUserId().equals(userId))
                        .collect(Collectors.toList())
                );
                break;
            case "共享文件夹":
                result.setFile(byUserIdAndFather.stream().filter(it->it.isShare()).collect(Collectors.toList()));
                break;
            case "所有文件夹":
                result.setFile(byUserIdAndFather.stream().filter(it->!it.isShare()&&"folder".equals(it.getType())).collect(Collectors.toList()));
                break;
        }
        if (nowPath!=null){
            result.setNowFile(flDao.selectByPrimaryKey(nowPath));
        }
        List<File> sortedFiles = result.getFile().stream()
                .sorted(Comparator.comparing(file -> file.getType().equals("folder") ? 0 : 1))
                .collect(Collectors.toList());

        result.setFile(sortedFiles);
        return result;
    }

    @Override
    public boolean drop(String fileIds) {
        for (Long fileId : Arrays.stream(fileIds.split(",")).filter(it->it!=null).map(Long::valueOf).collect(Collectors.toList())) {
            File file = flDao.selectByPrimaryKey(fileId);
            file.setFileInTrash(true);
            file.setShare(false);
            flDao.updateByPrimaryKeySelective(file);
            if ("folder".equals(file.getType())){
                Example example = new Example(File.class);
                example.createCriteria().andEqualTo("father", file.getFileId());
                List<File> byFather = flDao.selectByExample(example);
                example.clear();
                if (!byFather.isEmpty()){
                    byFather.forEach(it->drop(String.valueOf(it.getFileId())));
                }
            }
        }
        return true;
    }

    @Override
    public boolean reDrop(String fileIds) {
        Arrays.stream(fileIds.split(",")).filter(it->it!=null).map(Long::valueOf).forEach(it->{
            File file = flDao.selectByPrimaryKey(it);
            file.setFileInTrash(false);
            flDao.updateByPrimaryKeySelective(file);
        });
        return true;
    }

    @Override
    public boolean delete(String fileIds) {
        for (Long fileId : Arrays.stream(fileIds.split(",")).filter(it->it!=null).map(Long::valueOf).collect(Collectors.toList())) {
            File fileList = flDao.selectByPrimaryKey(fileId);
            java.io.File file = new java.io.File(this.rootPath,fileList.getFilePath());
            if(file.exists()&&file.isFile()){
                System.out.println("现在删除"+fileList.getFileName()+"数据库存档>>>>>>>>>");
                flDao.delete(fileList);
                System.out.println("现在删除"+fileList.getFileName()+"本地文件>>>>>>>>>");
                file.delete();
            }
            flDao.delete(fileList);
            if ("folder".equals(fileList.getType())){
                Example example = new Example(File.class);
                example.createCriteria().andEqualTo("father", fileList.getFileId());
                List<File> byFather = flDao.selectByExample(example);
                example.clear();
                if (!byFather.isEmpty()){
                    byFather.forEach(it->delete(String.valueOf(it.getFileId())));
                }
            }
        }
        return true;
    }

    @Override
    public boolean rename(String fileId, String newName) {
        try {
            Arrays.stream(fileId.split(",")).map(Long::valueOf).forEach(it->{
                File file = flDao.selectByPrimaryKey(fileId);
                file.setFileName(newName);
                flDao.updateByPrimaryKeySelective(file);
            });
        }catch (Exception e){
            System.out.println(e.toString());
            return false;
        }
        return true;
    }

    @Override
    public boolean moveFile(String fileId, Long newFatherId) {
        try {
            Arrays.stream(fileId.split(",")).map(Long::valueOf).forEach(it->{
                File file = flDao.selectByPrimaryKey(fileId);
                file.setFather(newFatherId);
                flDao.updateByPrimaryKeySelective(file);
            });
        }catch (Exception e){
            System.out.println(e);
            return false;
        }
        return true;
    }

    @Override
    public boolean shareFile(String fileId,String sharePerson){
        User user = UserTokenHolder.getUser();
        Example example = new Example(File.class);
        example.createCriteria().andIn("fileId",Arrays.asList(fileId.split(",")));
        Department departmentById = departmentService.findDepartmentById(Long.valueOf(user.getDeptId().split(",")[0]));
        User manager = userService.findById(departmentById.getManagerId());

        flDao.selectByExample(example).forEach(it->{
            File file = flDao.selectByPrimaryKey(fileId);
            file.setStatus(manager==null?0:1);
            if (it.isShare())throw new ServiceException(it.getFileName()+"已分享");
            if (manager==null){
                if (sharePerson==null){
                    throw new ServiceException("请选择分享人");
                }
                file.setSharePeople(sharePerson);
            } else {
                FileAuditRecord fileAuditRecord = new FileAuditRecord();
                fileAuditRecord.setFileId(file.getFileId());
                fileAuditRecord.setSubmitTime(new Date());
                fileAuditRecord.setFileName(file.getFileName());
                fileAuditRecord.setSubmitUserName(user.getUserName());
                fileAuditRecord.setSubmitUserId(user.getId());
                fileAuditRecord.setPersonInCharge(1L);
                fileAuditRecord.setPersonInChargeName("admin");
                fileAuditRecordService.saveFileAuditRecord(fileAuditRecord);
            }
            flDao.updateByPrimaryKeySelective(file);
        });
        return true;
    }

    /**
     * 得到文件
     * @param filepath
     * @return
     */
    public java.io.File getFile(String filepath){
        return new java.io.File(this.rootPath,filepath);
    }
}