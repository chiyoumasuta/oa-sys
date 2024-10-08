package cn.gson.oasys.listener;

import org.flowable.engine.RuntimeService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.flowable.engine.impl.el.FixedValue;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * 会签监听器示例
 *
 * @author: linjinp
 * @create: 2019-11-18 11:43
 **/
@Component
public class ListenerDemo implements ExecutionListener {

    // 页面配置参数注入
    private FixedValue num;

    @Resource
    private RuntimeService runtimeService;

    private static ListenerDemo listenerDemo;

    // 解决监听器中 Bean 获取不到问题
    @PostConstruct
    public void init() {
        listenerDemo = this;
        listenerDemo.runtimeService = this.runtimeService;
    }

    @Override
    public void notify(DelegateExecution delegateExecution) {
        // 调用 runtimeService 示例
        ProcessInstance processInstance = listenerDemo.runtimeService.createProcessInstanceQuery().processInstanceId(delegateExecution.getProcessInstanceId()).singleResult();
        System.out.println(processInstance.getProcessDefinitionId());

        // 获取页面配置参数的值
        System.out.println(num.getExpressionText());

        // 校验 okNum 是否已经存在
        if (!delegateExecution.hasVariable("okNum")) {
            delegateExecution.setVariable("okNum", 0);
        }
        // 已审核次数，审核一次 +1
        int okNum = (int) delegateExecution.getVariable("okNum") + 1;
        delegateExecution.setVariable("okNum", okNum);
    }
}
