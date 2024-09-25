package cn.gson.oasys.service.impl;

import cn.gson.oasys.dao.UserDao;
import cn.gson.oasys.entity.Department;
import cn.gson.oasys.entity.User;
import cn.gson.oasys.service.DepartmentService;
import cn.gson.oasys.service.FlowableUserService;
import cn.gson.oasys.service.UserService;
import cn.gson.oasys.support.Page;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang.StringUtils;
import org.flowable.engine.IdentityService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngines;
import org.hibernate.service.spi.ServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    @Resource
    private UserDao userDao;
    @Value("${user.password}")
    private String defaultUserPwd;
    @Resource
    private DepartmentService departmentService;
    @Resource
    private FlowableUserService flowableUserService;

    @Override
    public Page<User> page(String name, String phone, String roleName, int pageNo, int pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        Example example = new Example(User.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("del", false);
        if (StringUtils.isNotBlank(name)) {
            criteria.andLike("userName", "%" + name + "%");
        }
        if (StringUtils.isNotBlank(phone)) {
            criteria.andLike("phone", "%" + phone + "%");
        }
        com.github.pagehelper.Page<User> pageInfo = (com.github.pagehelper.Page) userDao.selectByExample(example);
        List<User> lists = pageInfo.getResult().stream().map(it->{
            String deptName = "";
            AtomicBoolean isMannger = new AtomicBoolean(false);
            if (it.getDeptId()!=null){
                deptName = Arrays.stream(it.getDeptId().split(",")).filter(v->v!=null).map(d->{
                    Department departmentById = departmentService.findDepartmentById(Long.valueOf(d));
                    if (departmentById!=null&&departmentById.getManagerId().equals(it.getId())) isMannger.set(true);
                    return departmentById.getName();
                }).collect(Collectors.joining(","));
            }
            it.setDeptName(deptName);
            it.setManager(isMannger.get());
            return it;
        }).collect(Collectors.toList());
        if (lists.isEmpty()) {
            return new Page<>();
        }
        return new Page<>(pageNo, pageSize, pageInfo.getTotal(), lists);
    }

    @Override
    public List<User> findDetailByIds(List<Long> userIds) {
        Example example = new Example(User.class);
        example.createCriteria().andIn("id", userIds);
        return userDao.selectByExample(example);
    }

    @Override
    public void saveOrUpdate(User user) {
        if (user.getId() != null && user.getId() == 1l) {
            throw new ServiceException("超级管理员拥有所有权限，不允许操作");
        }

//        user.setDeptId(String.valueOf(deptId));
        user.setPassword("Xiongbo99");
        User oldPhone = findByPhone(user.getPhone());
        if (user.getId() == null) {
            if (oldPhone != null) {
                throw new ServiceException("手机号已经存在[" + user.getPhone() + "]");
            }
            user.setPassword(defaultUserPwd);
            userDao.insert(user);
            //维护流程系统用户
//            flowableUserService.createUser(user.getUserName());
        } else {
            userDao.updateByPrimaryKeySelective(user);
        }
    }

    /**
     * 维护用户
     */
    @Test
    public void createUser() {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        // 通过 IdentityService 完成相关的用户和组的管理
        IdentityService identityService = processEngine.getIdentityService();
        org.flowable.idm.api.User user = null;
        for (User u : userDao.selectAll()) {
            user = identityService.newUser(u.getUserName());
            user.setFirstName(u.getUserName());
            user.setEmail(u.getUserName());
            identityService.saveUser(user);
        }
    }


    @Override
    public void del(Long id) {
        if (id == 1l) {
            throw new ServiceException("超级管理员拥有所有权限，不允许操作");
        }
        User oldUser = userDao.selectByPrimaryKey(id);
        if (oldUser == null) {
            throw new ServiceException("当前操作用户不存在");
        }
//        User currentUser = UserTokenHolder.getUser();
//        if (oldUser.getId().equals(currentUser.getId())) {
//            throw new ServiceException("不允许操作自己");
//        }
        oldUser.setDel(true);
        oldUser.setPhone(oldUser.getPhone() + "[delete]");
        userDao.updateByPrimaryKeySelective(oldUser);
    }


    @Override
    public User verifyByPhone(String phone, String password) {
        User user = findByPhone(phone);
        if (user == null || !password.equals(user.getPassword())) {
            return null;
        }
        String token = Base64.getEncoder().encodeToString(user.getPhone().getBytes());
        user.setToken(token);
        user.setLoginAt(new Date());
        userDao.updateByPrimaryKeySelective(user);
        return user;
    }

    @Override
    public User verifyAndGetUser(String phone, String passWord) {
        Example example = new Example(User.class);
        example.createCriteria().andEqualTo("phone", phone).andEqualTo("del", false);
        User user = userDao.selectOneByExample(example);
        if (user == null || !user.getPassword().equals(passWord)) return null;
        user.setLoginAt(new Date());
        userDao.updateByPrimaryKeySelective(user);
        return user;
    }

    @Override
    public boolean resetPwd(Long id) {
        if (id == 1l) {
            throw new ServiceException("超级管理员拥有所有权限，不允许操作");
        }
        User oldUser = userDao.selectByPrimaryKey(id);
        if (oldUser == null) {
            throw new ServiceException("当前操作用户不存在");
        }
        oldUser.setPassword(defaultUserPwd);
        return userDao.updateByPrimaryKeySelective(oldUser) > 0;
    }

    @Override
    public boolean changePwd(String phone, String oldpwd, String newpwd) {
        User oldUser = findByPhone(phone);
        if (oldUser == null) {
            throw new ServiceException("当前操作用户不存在");
        }
        if (oldUser.getId() == 1l) {
            throw new ServiceException("超级管理员拥有所有权限，不允许操作");
        }
        if (!oldUser.getPassword().equals(oldpwd)) {
            return false;
        }
        if (oldUser.getPassword().equals(newpwd)) {
            throw new ServiceException("新密码不能和旧密码一样");
        }
        oldUser.setPassword(newpwd);
        userDao.updateByPrimaryKeySelective(oldUser);
        return true;
    }

    @Override
    public boolean changePwd(String phone, String newPwd) {
        User oldUser = findByPhone(phone);
        if (oldUser == null) {
            throw new ServiceException("当前操作用户不存在");
        }
        if (oldUser.getId() == 1l) {
            throw new ServiceException("超级管理员拥有所有权限，不允许操作");
        }
        if (oldUser.getPassword().equals(newPwd)) {
            throw new ServiceException("旧密码和新密码不能相同");
        }
        oldUser.setPassword(newPwd);
        userDao.updateByPrimaryKeySelective(oldUser);
        return true;
    }

    @Override
    public User findByLoginName(String loginName) {
        Example example = new Example(User.class);
        example.createCriteria().andEqualTo("loginName", loginName);
        return userDao.selectOneByExample(example);
    }

    @Override
    public User getPermsByUser(User user, Integer clientType) {
//        List<SysRole> sysRoles = sysUserRoleService.getRoles(user);
//        if (null != sysRoles) {
//            user.setRoles(sysRoles.stream().map(it -> {
//                SysRole sysRole = new SysRole();
//                sysRole.setId(it.getId());
//                sysRole.setRoleName(it.getRoleName());
//                sysRole.setRoleKey(it.getRoleKey());
//                return sysRole;
//            }).collect(Collectors.toList()));
//        }
//        List<SysMenu> menus = sysMenuService.getMenuTreeByUser(user, clientType);
//        user.setMenus(sysMenuService.buildMenus(menus));
//        user.setPermissions(sysRoleMenuService.getMenuPermsByUser(user, null));
//        user.setPassword(null);
        return user;
//        return null;
    }

    @Override
    public User findByToken(String token) {
        Example example = new Example(User.class);
        example.createCriteria().andEqualTo("token", token);
        User user = userDao.selectOneByExample(example);
        if (user != null) {
//            user.setRoles(sysUserRoleService.getRoles(user));
            return user;
        }
        return null;
    }

    @Override
    public User findByPhone(String phone) {
        if (StringUtils.isEmpty(phone)) {
            return null;
        }
        Example example = new Example(User.class);
        example.createCriteria().andEqualTo("phone", phone);
        return userDao.selectOneByExample(example);
    }

    @Override
    public Map<Long, User> getAppUserMapUserNameAndPhone() {
        Example example = new Example(User.class);
        example.selectProperties("id", "userName", "phone");
        return userDao.selectByExample(example).stream().collect(Collectors.toMap(User::getId, it -> it));
    }

    @Override
    public User findById(Long userId) {
        return userDao.selectByPrimaryKey(userId);
    }

    @Override
    public List<User> findAllByDeptId(Long deptId) {
        Example example = new Example(User.class);
        example.createCriteria().andLike("deptId", "%"+deptId+"%");
        return userDao.selectByExample(example);
    }

    /**
     * 通过Id查询用户
     *
     * @param ids
     */
    @Override
    public List<User> findByIds(String ids) {
        Example example = new Example(User.class);
        example.createCriteria().andIn("id", Arrays.asList(ids.split(",")));
        return userDao.selectByExample(example);
    }
}
