package cn.gson.oasys.service.impl;

import cn.gson.oasys.dao.PpsDao;
import cn.gson.oasys.dao.PpsItemDao;
import cn.gson.oasys.entity.Pps;
import cn.gson.oasys.entity.PpsItem;
import cn.gson.oasys.entity.User;
import cn.gson.oasys.service.DepartmentService;
import cn.gson.oasys.service.PpsService;
import cn.gson.oasys.service.UserService;
import cn.gson.oasys.support.Page;
import cn.gson.oasys.support.UserTokenHolder;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PpsServiceImpl implements PpsService {

    @Resource
    private PpsDao ppsDao;
    @Resource
    private PpsItemDao ppsItemDao;
    @Resource
    private DepartmentService departmentService;
    @Resource
    private UserService userService;

    /**
     * 分页查询
     *
     * @param pageSize
     * @param pageNo
     * @param projectName
     */
    @Override
    public Page<Pps> page(int pageSize, int pageNo, String projectName) {
        PageHelper.startPage(pageNo, pageSize);
        Example example  = new Example(Pps.class);
        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(projectName)) {
            criteria.andEqualTo("projectName", projectName);
        }
        criteria.andEqualTo("invalid",false);
        com.github.pagehelper.Page<Pps> page = (com.github.pagehelper.Page) ppsDao.selectByExample(example);
        List<Pps> collect = page.getResult().stream().map(it -> {
            it.setPpsItems(findByPpsId(it.getId()));
            return it;
        }).collect(Collectors.toList());
        return new Page<>(pageNo,pageSize,page.getTotal(),collect);
    }

    /**
     * 添加/更新
     *
     * @param pps
     */
    @Override
    public boolean saveOrUpdate(Pps pps) {
        User user = UserTokenHolder.getUser();
        if (pps.getId() == null) {
            if (pps.getDeptId() == null) {
                throw new RuntimeException("未指定部门");
            }
            if (pps.getHeadId() == null) {
                throw new RuntimeException("未指定负责人");
            }
            pps.setHeadName(userService.findById(pps.getHeadId()).getUserName());
            pps.setCreateTime(new Date());
            pps.setCreateUser(user.getId());
            pps.setCreateUserName(user.getUserName());
            pps.setDeptName(departmentService.findDepartmentById(String.valueOf(pps.getDeptId())).get(0).getName());
            pps.setInvalid(false);
            return ppsDao.insert(pps)>0;
        }else return ppsDao.updateByPrimaryKeySelective(pps)>0;
    }

    /**
     * 添加进度
     *
     * @param item
     */
    @Override
    public boolean saveItem(PpsItem item) {
        if (item.getPpsId() == null) {
            throw new RuntimeException("未指定PpsId");
        }
        if (item.getInfo() == null) {
            throw new RuntimeException("请填写进度描述");
        }
        User user = UserTokenHolder.getUser();
        item.setCreateUser(user.getId());
        item.setCreateUserName(user.getUserName());
        item.setCreateTime(new Date());
        return ppsItemDao.insert(item)>0;
    }

    /**
     * 通过id删除进度更新
     *
     * @param id
     */
    @Override
    public boolean deleteItem(Long id) {
        return ppsItemDao.deleteByPrimaryKey(id)>0;
    }

    /**
     * 通过id删除项目进度统计
     *
     * @param id
     */
    @Override
    public boolean deletePps(Long id) {
        Pps pps = ppsDao.selectByPrimaryKey(id);
        pps.setInvalid(true);
        return ppsDao.updateByPrimaryKeySelective(pps)>0;
    }

    @Override
    public List<PpsItem> findByPpsId(Long id) {
        Example example = new Example(PpsItem.class);
        example.createCriteria().andEqualTo("ppsId", id);
        return ppsItemDao.selectByExample(example);
    }
}
