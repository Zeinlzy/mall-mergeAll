package com.lzy.mall.common.log;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONUtil;
import com.lzy.mall.common.domain.WebLog;
import com.lzy.mall.common.util.RequestUtil;
import io.swagger.v3.oas.annotations.Operation;
import net.logstash.logback.marker.Markers;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一日志处理切面
 * 定义了一个Spring AOP (面向切面编程) 切面，名为 WebLogAspect。它的主要作用是拦截并记录Web层（Controller层）的请求和响应信息。
 */
@Aspect // 声明这是一个切面类
@Component // 将该类注册为Spring容器中的一个Bean
@Order(1) // 定义切面的执行优先级，数字越小优先级越高
public class WebLogAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebLogAspect.class);

    /**
     * 定义切点 (Pointcut)
     * 拦截规则：
     * 1. com.lzy.mall.controller 包下所有类的所有公共方法
     * 2. com.lzy.mall.*.controller 包（例如 com.lzy.mall.user.controller）下所有类的所有公共方法
     * "execution(public * com.lzy.mall.controller.*.*(..))"
     *  - execution: AOP执行时机
     *  - public: 匹配公共方法
     *  - *: 匹配任意返回值类型
     *  - com.lzy.mall.controller.*: com.lzy.mall.controller包下的所有类
     *  - .*: 匹配类中的所有方法
     *  - (..): 匹配任意数量和类型的参数
     * "||" 表示 "或" 逻辑
     */
    @Pointcut("execution(public * com.lzy.mall.controller.*.*(..))||execution(public * com.lzy.mall.*.controller.*.*(..))")
    public void webLog() {
        // 这个方法仅作为切点的标识，方法体为空
    }

    /**
     * 前置通知 (Before Advice)
     * 在目标方法执行之前执行
     * 目前此方法为空，可以根据需要添加逻辑
     *
     * @param joinPoint 连接点，代表被拦截的方法
     * @throws Throwable 异常
     */
    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        // 可以在这里记录请求开始等信息，但当前版本核心逻辑在doAround中
    }

    /**
     * 后置返回通知 (AfterReturning Advice)
     * 在目标方法成功执行并返回结果之后执行
     * 目前此方法为空，可以根据需要添加逻辑
     *
     * @param ret 目标方法返回的结果
     * @throws Throwable 异常
     */
    @AfterReturning(value = "webLog()", returning = "ret")
    public void doAfterReturning(Object ret) throws Throwable {
        // 可以在这里记录响应结果等信息，但当前版本核心逻辑在doAround中
    }

    /**
     * 环绕通知 (Around Advice)
     * 包围一个连接点（被拦截的方法）的通知。这是功能最强大的通知类型。
     * 环绕通知可以在方法调用前后自定义行为，并且负责选择继续执行连接点或直接返回自定义的返回值或抛出异常。
     *
     * @param joinPoint 可用于执行目标方法的ProceedingJoinPoint对象
     * @return 目标方法的执行结果
     * @throws Throwable 异常
     */
    @Around("webLog()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 记录方法开始执行的时间戳
        long startTime = System.currentTimeMillis();

        // 2. 获取当前的HttpServletRequest对象
        // RequestContextHolder是Spring提供的工具类，用于在当前线程中获取请求相关的属性
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        // 创建WebLog对象，用于封装日志信息
        WebLog webLog = new WebLog();//记录请求信息(通过Logstash传入Elasticsearch)

        Object result = joinPoint.proceed();
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        if (method.isAnnotationPresent(Operation.class)) {
            Operation log = method.getAnnotation(Operation.class);
            webLog.setDescription(log.summary());
        }

        long endTime = System.currentTimeMillis();  // 记录方法结束执行时间

        String urlStr = request.getRequestURL().toString();
        webLog.setBasePath(StrUtil.removeSuffix(urlStr, URLUtil.url(urlStr).getPath()));
        webLog.setUsername(request.getRemoteUser());
        webLog.setIp(RequestUtil.getRequestIp(request));
        webLog.setMethod(request.getMethod());
        webLog.setParameter(getParameter(method, joinPoint.getArgs()));
        webLog.setResult(result);
        webLog.setSpendTime((int) (endTime - startTime));
        webLog.setStartTime(startTime);
        webLog.setUri(request.getRequestURI());
        webLog.setUrl(request.getRequestURL().toString());
        Map<String,Object> logMap = new HashMap<>();
        logMap.put("url",webLog.getUrl());
        logMap.put("method",webLog.getMethod());
        logMap.put("parameter",webLog.getParameter());
        logMap.put("spendTime",webLog.getSpendTime());
        logMap.put("description",webLog.getDescription());
//        LOGGER.info("{}", JSONUtil.parse(webLog));
        LOGGER.info(Markers.appendEntries(logMap), JSONUtil.parse(webLog).toString());
        return result;
    }

    /**
     * 根据方法和传入的参数获取请求参数
     */
    private Object getParameter(Method method, Object[] args) {
        List<Object> argList = new ArrayList<>();
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            //将RequestBody注解修饰的参数作为请求参数
            RequestBody requestBody = parameters[i].getAnnotation(RequestBody.class);
            if (requestBody != null) {
                argList.add(args[i]);
            }
            //将RequestParam注解修饰的参数作为请求参数
            RequestParam requestParam = parameters[i].getAnnotation(RequestParam.class);
            if (requestParam != null) {
                Map<String, Object> map = new HashMap<>();
                String key = parameters[i].getName();
                if (!StrUtil.isEmpty(requestParam.value())) {
                    key = requestParam.value();
                }
                if(args[i]!=null){
                    map.put(key, args[i]);
                    argList.add(map);
                }
            }
        }
        if (argList.size() == 0) {
            return null;
        } else if (argList.size() == 1) {
            return argList.get(0);
        } else {
            return argList;
        }
    }
}
