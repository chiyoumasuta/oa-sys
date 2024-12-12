package cn.gson.oasys.service.impl;

import cn.gson.oasys.dao.CustomerInformationDao;
import cn.gson.oasys.entity.CustomerInformation;
import cn.gson.oasys.entity.Pps;
import cn.gson.oasys.entity.User;
import cn.gson.oasys.service.CustomerInformationService;
import cn.gson.oasys.support.Page;
import cn.gson.oasys.support.UserTokenHolder;
import cn.gson.oasys.support.exception.ServiceException;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerInformationServiceImpl implements CustomerInformationService {

    @Resource
    private CustomerInformationDao customerInformationDao;

    @Override
    public Page<CustomerInformation> page(int pageNo, int pageSize, String contactPerson) {
        User user = UserTokenHolder.getUser();
        PageHelper.startPage(pageNo, pageSize);
        Example example = new Example(CustomerInformation.class);
        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(contactPerson)) {
            criteria.andEqualTo("contactPerson", contactPerson);
        }
        if (!user.isAdmin()){
            criteria.andEqualTo("updatePerson", user.getId());
        }
        com.github.pagehelper.Page<CustomerInformation> page = (com.github.pagehelper.Page<CustomerInformation>) customerInformationDao.selectByExample(example);
        return new Page<>(pageNo, pageSize, page.getTotal(), page.getResult());
    }

    @Override
    public boolean saveOrUpdateCustomerInformation(CustomerInformation customerInformation) {
        customerInformation.setUpdateTime(new Date());
        if (customerInformation.getId() == null) {
            User user = UserTokenHolder.getUser();
            customerInformation.setUpdatePerson(user.getId());
            customerInformation.setUpdatePersonName(user.getUserName());
            return customerInformationDao.insert(customerInformation)>0;
        }else {
            if (customerInformationDao.selectByPrimaryKey(customerInformation.getId())==null) {
                throw new ServiceException("参数错误");
            }
            return customerInformationDao.updateByPrimaryKeySelective(customerInformation)>0;
        }
    }

    @Override
    public boolean deleteById(Long id) {
        CustomerInformation customerInformation = customerInformationDao.selectByPrimaryKey(id);
        if (customerInformation == null) {
            return false;
        }else {
            if (customerInformation.getUpdatePerson().equals(UserTokenHolder.getUser().getId())||UserTokenHolder.isAdmin()) {
                return customerInformationDao.deleteByPrimaryKey(id)>0;
            }else {
                throw new ServiceException("当前用户无法删除");
            }
        }
    }
}
