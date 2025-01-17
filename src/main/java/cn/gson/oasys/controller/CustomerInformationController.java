package cn.gson.oasys.controller;

import cn.gson.oasys.entity.CustomerInformation;
import cn.gson.oasys.entity.CustomerInformationItem;
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
        return UtilResultSet.success(customerInformationService.page(pageNo,pageSize,contactPerson));
    }

    @RequestMapping(value = "/saveOrUpdate", method = RequestMethod.POST)
    @ApiOperation("新增/修改公司")
    public UtilResultSet saveOrUpdate(CustomerInformation customerInformation){
        if (customerInformationService.saveOrUpdateCustomerInformation(customerInformation)){
            return UtilResultSet.success("新增/修改成功");
        }else {
            return UtilResultSet.bad_request("新增/修改失败");
        }
    }


    @RequestMapping(value = "/saveOrUpdateItem", method = RequestMethod.POST)
    @ApiOperation("新增/修改公司")
    public UtilResultSet saveOrUpdate(CustomerInformationItem item){
        if (customerInformationService.saveOrUpdateItem(item)){
            return UtilResultSet.success("新增/修改成功");
        }else {
            return UtilResultSet.bad_request("新增/修改失败");
        }
    }

    @RequestMapping(value = "/deleteById",method = RequestMethod.POST)
    @ApiOperation(value = "删除",notes = "传入0为删除公司，1为删除联系方式")
    public UtilResultSet deleteById(Long id,int type){
        if (customerInformationService.deleteById(id,type)){
            return UtilResultSet.success("删除成功");
        }else return UtilResultSet.bad_request("删除失败");
    }
}
