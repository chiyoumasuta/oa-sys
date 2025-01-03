package cn.gson.oasys.service.impl;

import cn.gson.oasys.dao.UserDao;
import cn.gson.oasys.dao.UserDeptRoleDao;
import cn.gson.oasys.entity.Department;
import cn.gson.oasys.entity.User;
import cn.gson.oasys.entity.UserDeptRole;
import cn.gson.oasys.service.UserDeptRoleService;
import cn.gson.oasys.support.exception.ServiceException;
import cn.gson.oasys.service.DepartmentService;
import cn.gson.oasys.service.FlowableUserService;
import cn.gson.oasys.service.UserService;
import cn.gson.oasys.support.Page;
import cn.gson.oasys.support.exception.UnknownAccountException;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import javax.persistence.Transient;
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
    @Resource
    private UserDeptRoleService userDeptRoleService;
    @Autowired
    private UserDeptRoleDao userDeptRoleDao;

    @Override
    public Page<User> page(String name, String phone, int pageNo, int pageSize) {
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
        com.github.pagehelper.Page<User> pageInfo = (com.github.pagehelper.Page<User>) userDao.selectByExample(example);
        List<User> lists = pageInfo.getResult().stream().peek(it -> {
            String deptName = "";
            AtomicBoolean isMannger = new AtomicBoolean(false);
            List<UserDeptRole> itByUserId = userDeptRoleService.findRoleByUserId(it.getId());
            if (itByUserId.isEmpty()) {
                deptName = "";
            }else {
                deptName = itByUserId.stream().map(d -> {
                    Department departmentById = departmentService.findDepartmentById(String.valueOf(d.getDepartmentId())).get(0);
                    if (!d.getRole().equals("专员")) isMannger.set(true);
                    return departmentById.getName();
                }).collect(Collectors.joining(","));

                it.setDeptId(itByUserId.stream().map(UserDeptRole::getDepartmentId).map(Object::toString).collect(Collectors.joining(",")));
            }
            it.setDeptName(deptName);
            it.setManager(isMannger.get());
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
        if (user.getId() != null && user.getId() == 1L) {
            throw new ServiceException("超级管理员拥有所有权限，不允许操作");
        }

        user.setPassword(defaultUserPwd);
        User oldPhone = findByPhone(user.getPhone());
        if (user.getId() == null) {
            if (oldPhone != null) {
                throw new ServiceException("手机号已经存在[" + user.getPhone() + "]");
            }
            user.setPassword(defaultUserPwd);
            userDao.insert(user);
        } else {
            userDao.updateByPrimaryKeySelective(user);
        }
    }


    @Override
    @Transient
    public void del(Long id) {
        if (id == 1L) {
            throw new ServiceException("超级管理员拥有所有权限，不允许操作");
        }
        User oldUser = userDao.selectByPrimaryKey(id);
        if (oldUser == null) {
            throw new ServiceException("当前操作用户不存在");
        }
        oldUser.setDel(true);
        oldUser.setPhone(oldUser.getPhone() + "[delete]");
        Example example = new Example(UserDeptRole.class);
        example.createCriteria().andEqualTo("userId", oldUser.getId());
        userDeptRoleDao.deleteByExample(example);
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
        if (phone == null) {
            throw new ServiceException("请输入手机号");
        }
        example.createCriteria().andEqualTo("phone", phone).andEqualTo("del", false);
        User user = userDao.selectOneByExample(example);
        if (user == null || !user.getPassword().equals(passWord)) return null;
        user.setLoginAt(new Date());
        userDao.updateByPrimaryKeySelective(user);
        return user;
    }


    @Override
    public boolean resetPwd(Long id) {
        if (id == 1L) {
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
        if (oldUser.getId() == 1L) {
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
        if (oldUser.getId() == 1L) {
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
    public User findByToken(String token) {
        Example example = new Example(User.class);
        example.createCriteria().andEqualTo("token", token);
        User user = userDao.selectOneByExample(example);
        if (user != null) {
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
        User user = userDao.selectByPrimaryKey(userId);
        if (user == null) {
            throw new UnknownAccountException();
        }
        String deptName;
        AtomicBoolean isMannger = new AtomicBoolean(false);
        List<UserDeptRole> itByUserId = userDeptRoleService.findRoleByUserId(user.getId());
        if (itByUserId.isEmpty()) {
            deptName = "";
        }else {
            deptName = itByUserId.stream().map(d -> {
                Department departmentById = departmentService.findDepartmentById(String.valueOf(d.getDepartmentId())).get(0);
                if (!d.getRole().equals("专员")) isMannger.set(true);
                return departmentById.getName();
            }).collect(Collectors.joining(","));
            user.setDeptId(itByUserId.stream().map(UserDeptRole::getDepartmentId).map(Object::toString).collect(Collectors.joining(",")));
        }
        user.setDeptName(deptName);
        user.setManager(isMannger.get());
        user = userDeptRoleService.findUserRole(user);
        List<Department> byUserId = userDeptRoleService.findByUserId(user.getId());
        if (byUserId != null || !byUserId.isEmpty()) {
            user.setRoleList(byUserId.stream().map(Department::getRole).collect(Collectors.toList()));
        }
        return user;
    }

    /**
     * 通过Id查询用户
     *
     * @param ids
     */
    @Override
    public List<User> findByIds(String ids) {
        if (StringUtils.isEmpty(ids)) {
            return new ArrayList<>();
        } else {
            Example example = new Example(User.class);
            example.createCriteria().andIn("id", Arrays.asList(ids.split(",")));
            return userDao.selectByExample(example);
        }
    }
}
