package cn.gson.oasys.dao;

import cn.gson.oasys.entity.FileAuditRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface FileAuditRecordDao extends Mapper<FileAuditRecord> {
}
