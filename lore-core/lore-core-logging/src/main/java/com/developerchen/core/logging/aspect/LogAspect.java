package com.developerchen.core.logging.aspect;

import com.developerchen.core.common.util.JsonUtils;
import com.developerchen.core.common.util.RequestUtils;
import com.developerchen.core.logging.annotation.OperationLog;
import com.developerchen.core.logging.entity.Log;
import com.developerchen.core.logging.service.ILogService;
import jakarta.servlet.ServletResponse;
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
    @Pointcut("@annotation(com.developerchen.core.logging.annotation.OperationLog)")
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
        // 防止 ServletResponse 流被重复读取
        Object[] args = pjp.getArgs();
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof ServletResponse) {
                args[i] = null;
            }
        }

        String arguments = JsonUtils.toJsonString(args);

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
        OperationLog operationLogAnnotation = method.getAnnotation(OperationLog.class);
        String className = pjp.getTarget().getClass().getName();
        String methodName = signature.getName();

        Log log = new Log();
        log.setType(operationLogAnnotation.type());
        log.setDescription(operationLogAnnotation.desc());
        log.setRequestUri(RequestUtils.getRequestURI());
        log.setRequestQuery(RequestUtils.getRequestQueryString());
        log.setRequestMethod(RequestUtils.getRequestMethod());
        log.setMethod(className + "." + methodName + "()");
        log.setElapsedTime(elapsedTime);
        log.setIp(RequestUtils.getRemoteIp());
        log.setUserAgent(RequestUtils.getUserAgent());
        log.setArguments(arguments);

        if (exception != null) {
            log.setException(exception.getLocalizedMessage());
        }

        // 异步保存log
        logService.asyncSaveLog(log);
    }

}
