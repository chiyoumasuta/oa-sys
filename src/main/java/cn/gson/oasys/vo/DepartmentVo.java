package cn.gson.oasys.vo;

import cn.gson.oasys.entity.Department;
import cn.gson.oasys.entity.User;
import lombok.Data;

import java.util.List;

@Data
public class DepartmentVo extends Department {
    private List<User> users;
    private List<User> manager;
}
