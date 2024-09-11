package cn.gson.oasys.dao;

import cn.gson.oasys.entity.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;
import java.util.List;

public interface UserDao extends Mapper<User> {
    List<User> list(@Param("name") String name, @Param("phone") String phone, @Param("type") Integer type, @Param("notType") Integer notType, @Param("areas") List<String> areas, @Param("roleKey") String roleKey, @Param("ids") List<Long> ids, @Param("roleName") String roleName);
}
