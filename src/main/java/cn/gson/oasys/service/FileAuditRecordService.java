package cn.gson.oasys.service;

import cn.gson.oasys.entity.FileAuditRecord;
import cn.gson.oasys.support.Page;

import java.util.List;

public interface FileAuditRecordService {
    boolean saveFileAuditRecord(FileAuditRecord FileAuditRecord);
    boolean deleteFileAuditRecord(Long id);
    boolean updateFileAuditRecord(FileAuditRecord FileAuditRecord);
    FileAuditRecord findFileAuditRecordById(Long id);
    Page<FileAuditRecord> findAllFileAuditRecords(int pageNo,int pageSize,int searchType);
    /**
     * 文件审核
     */
    boolean audit(Long fileId,boolean result,String sharePeople);
}
