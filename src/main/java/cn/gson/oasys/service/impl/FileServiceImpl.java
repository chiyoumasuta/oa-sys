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
    @Autowired
    private FileDao fileDao;


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
    public FileListVo fileList(Long nowPath, String type,String tags) {
        Long userId = UserTokenHolder.getUser().getId();
        Long father = nowPath == null ? 0 : nowPath;
        Example example = new Example(File.class);
        if (tags!=null){
            example.createCriteria().andIn("tags", Arrays.asList(tags.split(",")));
        }
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
            case "共享文件夹":
                result.setFile(byUserIdAndFather.stream().filter(File::isShare).collect(Collectors.toList()));
                break;
            case "所有文件夹":
                List<File> collect = byUserIdAndFather.stream().filter(it ->"folder".equals(it.getType()) && !it.isFileInTrash()&&it.getUserId().equals(UserTokenHolder.getUser().getId())).collect(Collectors.toList());

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
        Set<String> tagSet = new HashSet<>();
        List<File> sortedFiles = result.getFile().stream().map(it->{
                    if(it.getTag()!=null&&!it.getTag().equals("")){
                        Set<String> oldTags = new HashSet<>(Arrays.asList(it.getTag().split(",")));
                        tagSet.addAll(oldTags);
                    }
                    return it;
                })
                .sorted(Comparator.comparing(file -> file.getType().equals("folder") ? 0 : 1))
                .collect(Collectors.toList());
        result.setFile(sortedFiles);
        result.setTags(tagSet);
        return result;
    }

    @Override
    public boolean drop(String fileIds) {
        for (Long fileId : Arrays.stream(fileIds.split(",")).filter(Objects::nonNull).map(Long::valueOf).collect(Collectors.toList())) {
            File file = flDao.selectByPrimaryKey(fileId);
            if (!file.getUserId().equals(UserTokenHolder.getUser().getId())||!UserTokenHolder.isAdmin()) {
                throw new ServiceException("非本人文件/管理员无法操作");
            }
            if (file.isFileInTrash()){
                return delete(String.valueOf(file.getFileId()));
            }else {
                file.setFileInTrash(true);
            }
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
        Example example = new Example(File.class);
        example.createCriteria().andIn("fileId", Arrays.asList(fileId.split(",")));
        example.createCriteria().andIn("fileId", Arrays.asList(fileId.split(",")));
        flDao.selectByExample(example).forEach(it -> {
            File file = flDao.selectByPrimaryKey(fileId);
            if (sharePerson == null) {
                throw new ServiceException("请选择分享人");
            }
            file.setShare(true);
            file.setSharePeople((file.getSharePeople() == null ? "" : file.getSharePeople() + ",") + sharePerson);
            flDao.updateByPrimaryKeySelective(file);
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

    @Override
    public boolean addTag(String tags, String ids) {
        if (ids==null){
            throw new ServiceException("标签和文件不能为空");
        }
        List<File> byIds = findByIds(Arrays.stream(ids.split(",")).map(Long::valueOf).collect(Collectors.toList()));
        Set<String> tagList = new HashSet<>(Arrays.asList(tags.split(",")));
        for (File file : byIds) {
            if (file.getTag()!=null){
                Set<String> oldTags = new HashSet<>(Arrays.asList(file.getTag().split(",")));
                tagList.addAll(oldTags);
            }
            file.setTag(String.join(",", tagList));
            fileDao.updateByPrimaryKeySelective(file);
        }
        return true;
    }

    @Override
    public boolean deleteTag(String tag,Long id) {
        File file = fileDao.selectByPrimaryKey(id);
        if (file==null){
            throw new ServiceException("文件标签错误");
        }
        Set<String> oldTags = new HashSet<>(Arrays.asList(file.getTag().split(",")));
        oldTags.remove(tag);
        if (oldTags.size()==0){
            file.setTag(null);
        }else {
            file.setTag(String.join(",", oldTags));
        }
        return fileDao.updateByPrimaryKey(file) > 0;
    }
}
