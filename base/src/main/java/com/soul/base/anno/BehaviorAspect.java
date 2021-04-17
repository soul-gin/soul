package com.soul.base.anno;

import com.soul.base.anno.disruptor.BehaviorEventProducer;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
* @author gin
* @date 2021/4/15
*/
@Component
@Aspect
public class BehaviorAspect {

    @Autowired
    private BehaviorEventProducer behaviorEventProducer;

    @Pointcut(value = "@annotation(com.soul.base.anno.BehaviorReport)")
    public void behavior() {}

    @After("behavior()")
    public void after(JoinPoint joinPoint) {
        Map<String, Object> reqMap = new HashMap<>(16);
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        reqMap.put("riskReportMethod", method.getName());
        String[] parameterNames = signature.getParameterNames();
        Object[] parameters = joinPoint.getArgs();
        if (null != parameterNames && parameterNames.length > 0){
            for (int i = 0; i < parameterNames.length; i++) {
                reqMap.put(parameterNames[i], parameters[i]);
            }
        }
        behaviorEventProducer.onData(reqMap);
    }
 
}