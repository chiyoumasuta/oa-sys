package cn.gson.oasys.service.impl;

import cn.gson.oasys.dao.FileAuditRecordDao;
import cn.gson.oasys.dao.FileDao;
import cn.gson.oasys.entity.File;
import cn.gson.oasys.entity.FileAuditRecord;
import cn.gson.oasys.exception.ServiceException;
import cn.gson.oasys.service.FileAuditRecordService;
import cn.gson.oasys.support.Page;
import cn.gson.oasys.support.UserTokenHolder;
import com.github.pagehelper.PageHelper;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class FileAuditRecordServiceImpl implements FileAuditRecordService {

    @Resource
    private FileAuditRecordDao fileAuditRecordDao;
    @Resource
    private FileDao flDao;


    @Override
    public boolean saveFileAuditRecord(FileAuditRecord FileAuditRecord) {
        FileAuditRecord.setResult("待审核");
        return fileAuditRecordDao.insert(FileAuditRecord) > 0;
    }

    @Override
    public boolean deleteFileAuditRecord(Long id) {
        FileAuditRecord fileAuditRecord = fileAuditRecordDao.selectByPrimaryKey(id);
        File file = flDao.selectByPrimaryKey(fileAuditRecord.getFileId());
        file.setStatus(0);
        flDao.updateByPrimaryKey(file);
        return fileAuditRecordDao.deleteByPrimaryKey(id) > 0;
    }

    @Override
    public boolean updateFileAuditRecord(FileAuditRecord FileAuditRecord) {
        return fileAuditRecordDao.updateByPrimaryKeySelective(FileAuditRecord) > 0;
    }

    @Override
    public FileAuditRecord findFileAuditRecordById(Long id) {
        return fileAuditRecordDao.selectByPrimaryKey(id);
    }

    @Override
    public Page<FileAuditRecord> findAllFileAuditRecords(int pageNo, int pageSize, int searchType) {
        Example example = new Example(FileAuditRecord.class);
        Long userId = UserTokenHolder.getUser().getId();
        if (searchType == 1) {//用户提交
            example.createCriteria().andEqualTo("submitUserId", userId);
        } else {//用户处理
            example.createCriteria().andEqualTo("personInCharge", userId);
        }
        PageHelper.startPage(pageNo, pageSize);
        com.github.pagehelper.Page<FileAuditRecord> pageInfo = (com.github.pagehelper.Page) fileAuditRecordDao.selectByExample(example);
        return new Page<>(pageNo, pageSize, pageInfo.getTotal(), pageInfo.getResult());
    }

    @Override
    public boolean audit(Long id, boolean result, String sharePeople) {
        FileAuditRecord fileAuditRecord = fileAuditRecordDao.selectByPrimaryKey(id);
        File file = flDao.selectByPrimaryKey(fileAuditRecord.getFileId());
        if (file == null) {
            throw new ServiceException("文件已被删除无需审核");
        }
        if (file.getStatus() != 1) {
            throw new ServiceException("当前状态无需审核");
        }
        if (result && sharePeople == null) {
            throw new ServiceException("请填写分享用户");
        }
        file.setStatus(result ? 2 : 3);
        file.setShare(result);
        fileAuditRecord.setAuditTime(new Date());
        fileAuditRecord.setResult("通过");
        file.setSharePeople((file.getSharePeople() == null ? "" : file.getSharePeople() + ",") + sharePeople);
        return flDao.updateByPrimaryKeySelective(file) > 0 && fileAuditRecordDao.updateByPrimaryKeySelective(fileAuditRecord) > 0;
    }
}
