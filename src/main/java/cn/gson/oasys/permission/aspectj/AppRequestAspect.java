package cn.gson.oasys.permission.aspectj;

import cn.gson.oasys.permission.RsaHelp;
import cn.gson.oasys.support.UserTokenHolder;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.hibernate.service.spi.ServiceException;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@Aspect
public class AppRequestAspect {

    private final String ExpGetResultDataPonit = "execution(* org.gcm.fiber.controller.app..*(..))";

    //定义切入点,拦截servie包其子包下的所有类的所有方法
//    @Pointcut("execution(* com.haiyang.onlinejava.complier.service..*.*(..))")
    //拦截指定的方法,这里指只拦截TestService.getResultData这个方法
    @Pointcut(ExpGetResultDataPonit)
    public void excuteService() {

    }

    /**
     * 环绕通知：
     * 环绕通知非常强大，可以决定目标方法是否执行，什么时候执行，执行时是否需要替换方法参数，执行完毕是否需要替换返回值。
     * 环绕通知第一个参数必须是org.aspectj.lang.ProceedingJoinPoint类型
     */
    //@Around(ExpGetResultDataPonit)
    @Around(ExpGetResultDataPonit)
    public Object doAroundAdvice(ProceedingJoinPoint joinPoint) {
        //System.out.println("环绕通知的目标方法名：" + joinPoint.getSignature().getName());
        try {
            Object obj = joinPoint.proceed(joinPoint.getArgs());//调用执行目标方法
            return processOutPutObj(joinPoint, obj);
        } catch (Throwable throwable) {
            throw new ServiceException(throwable.getMessage());
        }
    }

    private final static List<String> notControllerName = Arrays.asList("AppManageController");

    /**
     * 处理返回对象
     */
    private Object processOutPutObj(ProceedingJoinPoint joinPoint, Object obj) {
        //System.out.println("OBJ 原本为：" + obj.toString());
        HttpServletRequest request = UserTokenHolder.getRequest();
        Signature signature = joinPoint.getSignature();
        String className = signature.getDeclaringTypeName(); // 获取类名
        if (request.getHeader("token") != null && !notControllerName.contains(className)) {
            JSONObject result = new JSONObject();
            result.put("signResponse", RsaHelp.encryptByPrivateKey2(JSON.toJSONString(obj, SerializerFeature.DisableCircularReferenceDetect)));
            //System.out.println(result.getString("signResponse"));
            return result;
        }
        return obj;
    }

    /**
     * 处理输入参数
     */
    private Object[] processInputArg(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs(); // 获取方法的参数值数组
        if (joinPoint.getSignature() instanceof MethodSignature) {
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            if (Arrays.stream(methodSignature.getParameterNames()).filter(it -> "signRequest".equals(it)).collect(Collectors.toList()).size() > 0) {
                String[] parameterNames = methodSignature.getParameterNames(); // 获取参数名数组
                // 输出参数名和对应的参数值
                for (int i = 0; i < args.length; i++) {
                    String parameterName = parameterNames[i];
                    Object parameterValue = args[i];
                    if (parameterValue != null) {
                        args[i] = RsaHelp.decryptByPublicKeyKey2(parameterValue.toString());
                        System.out.println("解密参数名: " + parameterName + ", 解密参数值: " + args[i]);
                    }
                }
            }
        }
        return args;
    }
}
