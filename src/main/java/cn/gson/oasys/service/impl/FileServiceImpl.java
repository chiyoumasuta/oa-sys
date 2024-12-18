package cn.gson.oasys.service.impl;

import cn.gson.oasys.dao.FileDao;
import cn.gson.oasys.dao.UserDeptRoleDao;
import cn.gson.oasys.entity.*;
import cn.gson.oasys.entity.File;
import cn.gson.oasys.service.*;
import cn.gson.oasys.support.exception.ServiceException;
import cn.gson.oasys.support.UserTokenHolder;
import cn.gson.oasys.vo.FileListVo;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Resource
    private UserDeptRoleService userDeptRoleService;
    @Autowired
    private UserDeptRoleDao userDeptRoleDao;


    @Override
    public Long saveFile(MultipartFile file, Long nowPath, File.model model) throws IllegalStateException, IOException {
        User user = UserTokenHolder.getUser();
        if (user == null) {
            throw new ServerException("用户信息获取失败");
        }
        java.io.File savepath = new java.io.File(this.rootPath, user.getUserName());
        if (!savepath.exists()) {
            savepath.mkdirs();
        }

        String type = FilenameUtils.getExtension(file.getOriginalFilename());
        String newFileName = UUID.randomUUID().toString().toLowerCase() + "." + type;
        java.io.File targetFile = new java.io.File(savepath, newFileName);
        file.transferTo(targetFile);

        File fileDb = new File();
        String filename = file.getOriginalFilename();
        if (File.model.CLOUD.equals(model)) {
            Example example = new Example(File.class);
            example.createCriteria().andEqualTo("fileName", filename).andEqualTo("father", nowPath == null ? 0 : nowPath).andEqualTo("userId", user.getId());
            List<File> files = flDao.selectByExample(example);
            if (!files.isEmpty()) throw new ServiceException("文件名重复");
        }else if (File.model.REIMBURSEMENT.equals(model)) {
            fileDb.setFileInTrash(true);
        }

        fileDb.setFileName(filename);
        fileDb.setFilePath(targetFile.getAbsolutePath().replace("\\", "/").replace(this.rootPath, ""));
        fileDb.setType(type);
        fileDb.setModel(model == null ? File.model.CLOUD : model);
        fileDb.setFather(nowPath == null ? 0 : nowPath);
        fileDb.setSize(file.getSize());
        fileDb.setUploadTime(new Date());
        fileDb.setContentType(file.getContentType());
        fileDb.setUserId(user.getId());
        flDao.insert(fileDb);

        return fileDb.getFileId();
    }

    @Override
    public boolean makeFolder(Long nowPath, String name) {
        User user = UserTokenHolder.getUser();
        java.io.File savepath = new java.io.File(this.rootPath, user.getUserName());
        java.io.File folder = new java.io.File(savepath, name);
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                return false;
            }
        }
        Example example = new Example(File.class);
        example.createCriteria().andEqualTo("fileName", name)
                .andEqualTo("father", nowPath == null ? 0 : nowPath)
                .andEqualTo("userId", user.getId()).andEqualTo("type", "folder");
        List<File> files = flDao.selectByExample(example);
        if (!files.isEmpty()) throw new ServiceException("文件夹名重复");

        File filelist = new File();
        filelist.setFileName(name);
        filelist.setFilePath(folder.getAbsolutePath().replace("\\", "/").replace(this.rootPath, ""));
        filelist.setType("folder");
        filelist.setFather(nowPath == null ? 0 : nowPath);
        filelist.setUploadTime(new Date());
        filelist.setUserId(user.getId());
        filelist.setModel(File.model.CLOUD);
        flDao.insert(filelist);
        return true;
    }

    @Override
    public FileListVo fileList(Long nowPath, String type) {
        Long userId = UserTokenHolder.getUser().getId();
        Long father = nowPath == null ? 0 : nowPath;
        Example example = new Example(File.class);
        if ("回收站".equals(type) || "共享文件夹".equals(type)) {
            example.createCriteria().andLike("sharePeople", "%" + userId + "%").orEqualTo("userId", userId);
        } else {
            example.createCriteria().andEqualTo("father", father);
            Example.Criteria criteria = example.createCriteria();
            criteria.andLike("sharePeople", "%" + userId + "%").orEqualTo("userId", userId);
            example.and(criteria);
        }
        List<File> byUserIdAndFather = flDao.selectByExample(example);
        FileListVo result = new FileListVo();
        type = type == null ? "所有文件" : type;
        switch (type) {
            case "所有文件":
                result.setFile(byUserIdAndFather.stream()
                        .filter(it -> !it.isFileInTrash() && it.getUserId().equals(userId) && it.getModel().equals(File.model.CLOUD))
                        .collect(Collectors.toList()));
                break;
            case "回收站":
                result.setFile(byUserIdAndFather.stream()
                        .filter(it -> it.isFileInTrash() && Objects.equals(it.getUserId(), userId) && it.getModel().equals(File.model.CLOUD))
                        .collect(Collectors.toList()));
                break;
            case "报销附件":
                result.setFile(byUserIdAndFather.stream()
                        .filter(it -> it.getModel().equals(File.model.REIMBURSEMENT) && it.getUserId().equals(userId))
                        .collect(Collectors.toList()));
                break;
            case "待审核":
                result.setFile(byUserIdAndFather.stream()
                        .filter(it -> (it.getStatus() == 1 || it.getStatus() == 2 || it.getStatus() == 3) && it.getUserId().equals(userId) && it.getModel().equals(File.model.CLOUD))
                        .collect(Collectors.toList())
                );
                break;
            case "共享文件夹":
                result.setFile(byUserIdAndFather.stream().filter(File::isShare).collect(Collectors.toList()));
                break;
            case "所有文件夹":
                List<File> collect = byUserIdAndFather.stream().filter(it -> !it.isShare() && "folder".equals(it.getType())).collect(Collectors.toList());

//                Map<Long, File> fileMap = new HashMap<>();
//                List<File> roots = new ArrayList<>();
//
//                // Populate the map
//                for (File file : collect) {
//                    fileMap.put(file.getFileId(), file);
//                }
//
//                // Build the tree
//                for (File file : collect) {
//                    Long parentId = file.getFather();
//                    if (parentId == null || !fileMap.containsKey(parentId)) {
//                        roots.add(file); // No parent, so it's a root
//                    } else {
//                        File parent = fileMap.get(parentId);
//                        parent.getFiles().add(file);
//                    }
//                }
//                result.setFile(roots);
                result.setFile(collect);
                break;
        }
        if (nowPath != null) {
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
        for (Long fileId : Arrays.stream(fileIds.split(",")).filter(Objects::nonNull).map(Long::valueOf).collect(Collectors.toList())) {
            File file = flDao.selectByPrimaryKey(fileId);
            file.setFileInTrash(true);
            file.setShare(false);
            flDao.updateByPrimaryKeySelective(file);
            if ("folder".equals(file.getType())) {
                Example example = new Example(File.class);
                example.createCriteria().andEqualTo("father", file.getFileId());
                List<File> byFather = flDao.selectByExample(example);
                example.clear();
                if (!byFather.isEmpty()) {
                    byFather.forEach(it -> drop(String.valueOf(it.getFileId())));
                }
            }
        }
        return true;
    }

    @Override
    public boolean reDrop(String fileIds) {
        Arrays.stream(fileIds.split(",")).filter(Objects::nonNull).map(Long::valueOf).forEach(it -> {
            File file = flDao.selectByPrimaryKey(it);
            file.setFileInTrash(false);
            flDao.updateByPrimaryKeySelective(file);
        });
        return true;
    }

    @Override
    public boolean delete(String fileIds) {
        for (Long fileId : Arrays.stream(fileIds.split(",")).filter(Objects::nonNull).map(Long::valueOf).collect(Collectors.toList())) {
            File fileList = flDao.selectByPrimaryKey(fileId);
            java.io.File file = new java.io.File(this.rootPath, fileList.getFilePath());
            if (file.exists() && file.isFile()) {
                flDao.delete(fileList);
                file.delete();
            }
            flDao.delete(fileList);
            if ("folder".equals(fileList.getType())) {
                Example example = new Example(File.class);
                example.createCriteria().andEqualTo("father", fileList.getFileId());
                List<File> byFather = flDao.selectByExample(example);
                example.clear();
                if (!byFather.isEmpty()) {
                    byFather.forEach(it -> delete(String.valueOf(it.getFileId())));
                }
            }
        }
        return true;
    }

    @Override
    public boolean rename(String fileId, String newName) {
        try {
            Arrays.stream(fileId.split(",")).map(Long::valueOf).forEach(it -> {
                File file = flDao.selectByPrimaryKey(fileId);
                file.setFileName(newName);
                flDao.updateByPrimaryKeySelective(file);
            });
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
        return true;
    }

    @Override
    public boolean moveFile(String fileId, Long newFatherId) {
        try {
            Arrays.stream(fileId.split(",")).map(Long::valueOf).forEach(it -> {
                File file = flDao.selectByPrimaryKey(fileId);
                file.setFather(newFatherId);
                flDao.updateByPrimaryKeySelective(file);
            });
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
        return true;
    }

    @Override
    public boolean shareFile(String fileId, String sharePerson) {
        User user = userService.findById(UserTokenHolder.getUser().getId());
        Example example = new Example(File.class);
        example.createCriteria().andIn("fileId", Arrays.asList(fileId.split(",")));
        List<UserDeptRole> userDeptRoles;
        userDeptRoles = userDeptRoleService.findItByUserId(user.getId());

        flDao.selectByExample(example).forEach(it -> {
            File file = flDao.selectByPrimaryKey(fileId);
            if (it.isShare()) throw new ServiceException(it.getFileName() + "已分享");
            if (file.isShare() || user.isManager()) {
                file.setStatus(0);
                if (sharePerson == null) {
                    throw new ServiceException("请选择分享人");
                }
                file.setShare(true);
                file.setSharePeople((file.getSharePeople() == null ? "" : file.getSharePeople() + ",") + sharePerson);
            } else {
                file.setStatus(1);
                FileAuditRecord fileAuditRecord = new FileAuditRecord();
                fileAuditRecord.setFileId(file.getFileId());
                fileAuditRecord.setSubmitTime(new Date());
                fileAuditRecord.setFileName(file.getFileName());
                fileAuditRecord.setSubmitUserName(user.getUserName());
                fileAuditRecord.setSubmitUserId(user.getId());
                User personInCharge;
                if (userDeptRoles.isEmpty()){
                    personInCharge = userService.findById(1L);
                }else {
                    List<User> userList = userDeptRoleService.findByDepartmentId(userDeptRoles.get(0).getDepartmentId())
                            .stream()
                            .filter(u -> u.getRole() != null && !u.getRole().equals("专员")).collect(Collectors.toList());
                    if (userList.isEmpty()){
                        personInCharge = userService.findById(1L);
                    }else {
                        personInCharge = userList.get(0);
                    }
                }
                fileAuditRecord.setPersonInCharge(personInCharge.getId());
                fileAuditRecord.setPersonInChargeName(personInCharge.getUserName());
                fileAuditRecordService.saveFileAuditRecord(fileAuditRecord);
                flDao.updateByPrimaryKeySelective(file);
            }
        });
        return true;
    }

    public java.io.File getFile(String filepath) {
        return new java.io.File(this.rootPath, filepath);
    }

    @Override
    public List<File> findByIds(List<Long> ids) {
        Example example = new Example(File.class);
        example.createCriteria().andIn("fileId", ids);
        return flDao.selectByExample(example);
    }
}
