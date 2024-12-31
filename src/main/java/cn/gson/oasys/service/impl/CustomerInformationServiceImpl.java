package cn.gson.oasys.service.impl;

import cn.gson.oasys.dao.CustomerInformationDao;
import cn.gson.oasys.dao.CustomerInformationItemDao;
import cn.gson.oasys.entity.CustomerInformation;
import cn.gson.oasys.entity.CustomerInformationItem;
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
    @Resource
    private CustomerInformationItemDao customerInformationItemDao;

    @Override
    public Page<CustomerInformation> page(int pageNo, int pageSize, String contactPerson) {
        User user = UserTokenHolder.getUser();
        PageHelper.startPage(pageNo, pageSize);
        Example example1 = new Example(CustomerInformation.class);
        example1.createCriteria().andEqualTo("isDelete", false);
        com.github.pagehelper.Page<CustomerInformation> page = (com.github.pagehelper.Page<CustomerInformation>) customerInformationDao.selectByExample(example1);
        List<CustomerInformation> result = page.getResult().stream().peek(it->{
            Example example = new Example(CustomerInformationItem.class);
            Example.Criteria criteria = example.createCriteria();
            if (StringUtils.isNotBlank(contactPerson)) {
                criteria.andEqualTo("contactPerson", contactPerson);
            }
            if (!user.isAdmin()){
                criteria.andEqualTo("updatePerson", user.getId());
            }
            criteria.andEqualTo("customerInformationId", it.getId());
            List<CustomerInformationItem> items = customerInformationItemDao.selectByExample(example);
            it.setItems(items);
        }).filter(it->contactPerson==null||it.getItems().isEmpty()).collect(Collectors.toList());
        return new Page<>(pageNo, pageSize, page.getTotal(), result);
    }

    @Override
    public boolean saveOrUpdateCustomerInformation(CustomerInformation customerInformation) {
        if (customerInformation.getId() == null) {
            User user = UserTokenHolder.getUser();
            return customerInformationDao.insert(customerInformation)>0;
        }else {
            if (customerInformationDao.selectByPrimaryKey(customerInformation.getId())==null) {
                throw new ServiceException("参数错误");
            }
            return customerInformationDao.updateByPrimaryKeySelective(customerInformation)>0;
        }
    }

    @Override
    public boolean saveOrUpdateItem(CustomerInformationItem item) {
        User user = UserTokenHolder.getUser();
        if (item.getId() == null) {
            item.setUpdatePerson(user.getId());
            item.setUpdateTime(new Date());
            item.setUpdatePersonName(user.getUserName());
            return customerInformationItemDao.insert(item)>0;
        }else {
            if (customerInformationItemDao.selectByPrimaryKey(item.getId())==null) {
                throw new ServiceException("参数错误");
            }
            item.setUpdatePerson(user.getId());
            item.setUpdateTime(new Date());
            item.setUpdatePersonName(user.getUserName());
            return customerInformationItemDao.updateByPrimaryKeySelective(item)>0;
        }
    }

    @Override
    public boolean deleteById(Long id,int type) {
        if (type == 0){
            CustomerInformation customerInformation = customerInformationDao.selectByPrimaryKey(id);
            if (customerInformation == null) {
                return false;
            }else {
                customerInformation.setDelete(true);
                return customerInformationDao.deleteByPrimaryKey(id)>0;
            }
        }else {
            CustomerInformationItem customerInformationItem = customerInformationItemDao.selectByPrimaryKey(id);
            if (customerInformationItem==null){
                return false;
            }else return customerInformationItemDao.deleteByPrimaryKey(id)>0;
        }
    }
}
