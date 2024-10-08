package cn.gson.oasys.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import org.flowable.ui.common.service.exception.InternalServerErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/app")
@Api(tags = "flowable汉化保留接口")
public class FlowableStencilSetResource {

    @Resource
    protected ObjectMapper objectMapper;

    @RequestMapping(value = "/rest/stencil-sets/editor", method = RequestMethod.GET, produces = "application/json")
    public JsonNode getStencilSetForEditor() {
        try {
            JsonNode stencilNode = objectMapper.readTree(this.getClass().getClassLoader().getResourceAsStream("stencilset/stencilset_bpmn.json"));
            return stencilNode;
        } catch (Exception e) {
            throw new InternalServerErrorException("Error reading bpmn stencil set json");
        }
    }

    @RequestMapping(value = "/rest/stencil-sets/cmmneditor", method = RequestMethod.GET, produces = "application/json")
    public JsonNode getCmmnStencilSetForEditor() {
        try {
            JsonNode stencilNode = objectMapper.readTree(this.getClass().getClassLoader().getResourceAsStream("stencilset/zh-CN.json"));
            return stencilNode;
        } catch (Exception e) {
            throw new InternalServerErrorException("Error reading bpmn stencil set json");
        }
    }
}
