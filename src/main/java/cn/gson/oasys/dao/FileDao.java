package cn.gson.oasys.dao;

import cn.gson.oasys.entity.File;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface FileDao extends Mapper<File> {
}