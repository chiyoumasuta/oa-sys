package cn.gson.oasys.service;

import cn.gson.oasys.entity.User;
import cn.gson.oasys.support.Page;

import java.util.List;
import java.util.Map;

public interface UserService {
    /**
     * 分页显示
     *
     * @param name     用户名
     * @param phone    电话
     * @param areas    区县
     * @param type     用户可登录类型 1平台 2APP 3全部
     * @param pageNo
     * @param pageSize
     * @return org.gcm.fiber.support.Page<org.gcm.fiber.bean.User>
     * @author 不愿透露
     * @date 2022/10/9 17:33
     */
    Page<User> page(String name, String phone, Integer type, List<String> areas, String roleName, int pageNo, int pageSize);

    /**
     * 通过id查询用户详情
     *
     * @param userIds
     * @return java.util.List<org.gcm.fiber.bean.User>
     * @author 不愿透露
     * @date 2023/3/23 14:16
     */
    List<User> findDetailByIds(List<Long> userIds);

    /**
     * 修改或添加用户
     *
     * @param id
     * @param userName  用户名
     * @param phone     电话
     * @param password  密码 不传不修改
     * @param roleIds   角色
     * @param fiberArea 所属管辖
     * @param type      1平台 2app 3全部
     * @author 不愿透露
     * @date 2022/10/9 17:33
     */
    void saveOrUpdate(Long id, String userName, String phone, String password, String roleIds, String fiberArea, Integer type, Long deptId, boolean inRole);

    /**
     * 删除用户 软删
     *
     * @param id
     * @author 不愿透露
     * @date 2022/10/9 17:34
     */
    void del(Long id);

    /**
     * 通过手机号和密码登录 并更新token
     *
     * @param phone
     * @param password
     * @return java.lang.String  返回token
     * @author 不愿透露
     * @date 2022/10/9 17:00
     */
    User verifyByPhone(String phone, String password);

    /**
     * @updateBy wwj[yeshengkele@outlook.com]
     * @description 校验用户并返回用户，返回为null代表校验失败
     */
    User verifyAndGetUser(String phone, String passWord);

    /**
     * 重置密码
     *
     * @param id
     * @author 不愿透露
     * @date 2022/10/9 17:34
     */
    boolean resetPwd(Long id);

    /**
     * 修改密码
     *
     * @param phone
     * @param oldpwd
     * @param newpwd
     * @return boolean
     * @author 不愿透露
     * @date 2022/10/9 17:35
     */
    boolean changePwd(String phone, String oldpwd, String newpwd);

    boolean changePwd(String phone, String newPwd);

    /**
     * 根据登录名查询用户 不存在返回new User();
     *
     * @param loginName
     * @return org.gcm.fiber.bean.User
     * @author 不愿透露
     * @date 2022/8/17 9:34
     */
    User findByLoginName(String loginName);

    /**
     * 查询用户的对应权限 并封装返回
     *
     * @param user
     * @param clientType 客户端类型 -1为pc -2为app  0为全部
     * @return org.gcm.fiber.bean.User
     * @author 不愿透露
     * @date 2022/8/16 16:51
     */
    User getPermsByUser(User user, Integer clientType);

    /**
     * 通过token查询用户
     *
     * @param token
     * @return org.gcm.fiber.bean.User
     * @author 不愿透露
     * @date 2022/10/9 16:55
     */
    User findByToken(String token);

    /**
     * 通过手机号查询
     *
     * @param phone
     * @return org.gcm.fiber.bean.User
     * @author 不愿透露
     * @date 2022/10/9 17:02
     */
    User findByPhone(String phone);

    /**
     * 获取用户 包含名称和电话
     *
     * @return java.util.Map<java.lang.Long, java.lang.String>
     * @author 不愿透露
     * @date 2022/10/11 17:35
     */
    Map<Long, User> getAppUserMapUserNameAndPhone();

    /**
     * 通过id查找
     *
     * @param userId
     * @return org.gcm.fiber.bean.User
     * @author 不愿透露
     * @date 2022/10/19 10:33
     */
    User findById(Long userId);
    /**
     * 查询部门所有用户
     */
    List<User> findAllByDeptId(Long deptId);
}
