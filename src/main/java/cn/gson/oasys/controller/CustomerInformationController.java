package cn.gson.oasys.controller;

import cn.gson.oasys.entity.CustomerInformation;
import cn.gson.oasys.service.CustomerInformationService;
import cn.gson.oasys.support.UtilResultSet;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/CustomerInformation")
@Api("客户信息想关接口")
public class CustomerInformationController {
    @Resource
    private CustomerInformationService customerInformationService;

    @RequestMapping(value = "/page", method = RequestMethod.POST)
    @ApiOperation("分页查询客户数据")
    public UtilResultSet page(int pageSize,int pageNo,String contactPerson){
        return UtilResultSet.success(customerInformationService.page(pageSize,pageNo,contactPerson));
    }

    @RequestMapping(value = "/saveOrUpdate", method = RequestMethod.POST)
    @ApiOperation("新增/修改客户数据")
    public UtilResultSet saveOrUpdate(CustomerInformation customerInformation){
        if (customerInformationService.saveOrUpdateCustomerInformation(customerInformation)){
            return UtilResultSet.success("新增/修改成功");
        }else {
            return UtilResultSet.bad_request("新增/修改失败");
        }
    }

    @RequestMapping(value = "/deleteById",method = RequestMethod.POST)
    @ApiOperation("删除")
    public UtilResultSet deleteById(Long id){
        if (customerInformationService.deleteById(id)){
            return UtilResultSet.success("删除成功");
        }else return UtilResultSet.bad_request("删除失败");
    }
}
