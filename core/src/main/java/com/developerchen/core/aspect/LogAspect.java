package com.developerchen.core.aspect;

import com.developerchen.core.annotation.LogInfo;
import com.developerchen.core.domain.entity.Log;
import com.developerchen.core.service.ILogService;
import com.developerchen.core.util.JsonUtils;
import com.developerchen.core.util.RequestUtils;
import com.developerchen.core.util.UserUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Log切面
 *
 * @author syc
 */
@Aspect
@Component
public class LogAspect {

    private final ILogService logService;


    public LogAspect(ILogService logService) {
        this.logService = logService;
    }

    /**
     * 日志切点
     */
    @Pointcut("@annotation(com.developerchen.core.annotation.LogInfo)")
    public void logPointcutDeclare() {
    }


    /**
     * 记录日志
     */
    @Around(value = "logPointcutDeclare()")
    public Object aroundAdvice(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        Object retVal = null;
        Throwable exception = null;
        // 目标方法调用前先获取方法参数，因为方法内可能会对参数修改
        String arguments = JsonUtils.toJsonString(pjp.getArgs());

        // 运行目标方法
        try {
            retVal = pjp.proceed();
        } catch (Throwable throwable) {
            exception = throwable;
        }

        // 方法执行耗时
        int elapsedTime = (int) (System.currentTimeMillis() - start);

        // 保存日志数据
        saveLog(pjp, arguments, elapsedTime, exception);

        if (exception != null) {
            throw exception;
        }
        return retVal;
    }

    /**
     * 保存日志
     *
     * @param pjp         ProceedingJoinPoint
     * @param arguments   方法参数
     * @param elapsedTime 方法执行耗时 ms
     * @param exception   异常信息
     */
    private void saveLog(ProceedingJoinPoint pjp, String arguments,
                         int elapsedTime, Throwable exception) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        LogInfo logAnnotation = method.getAnnotation(LogInfo.class);
        String className = pjp.getTarget().getClass().getName();
        String methodName = signature.getName();

        Log log = new Log();
        log.setType(logAnnotation.type());
        log.setDescription(logAnnotation.desc());
        log.setRequestUri(RequestUtils.getRequestURI());
        log.setRequestQuery(RequestUtils.getRequestQueryString());
        log.setRequestMethod(RequestUtils.getRequestMethod());
        log.setMethod(className + "." + methodName + "()");
        log.setElapsedTime(elapsedTime);
        log.setCreateUserId(UserUtils.getUserId());
        log.setIp(UserUtils.getRemoteIp());
        log.setUserAgent(UserUtils.getUserAgent());
        log.setArguments(arguments);

        if (exception != null) {
            log.setException(exception.getLocalizedMessage());
        }

        // 异步保存log
        logService.asyncSaveLog(log);
    }

}
