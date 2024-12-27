package cn.gson.oasys.service;

import cn.gson.oasys.entity.CustomerInformation;
import cn.gson.oasys.entity.CustomerInformationItem;
import cn.gson.oasys.support.Page;

public interface CustomerInformationService {
    Page<CustomerInformation> page(int pageNo, int pageSize,String contactPerson);
    boolean saveOrUpdateCustomerInformation(CustomerInformation customerInformation);
    boolean saveOrUpdateItem(CustomerInformationItem item);
    boolean deleteById(Long id,int type);
}
