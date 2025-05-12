package com.lzy.mall.security.aspect;

import com.lzy.mall.security.annotation.CacheException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * (1) 类的整体作用说明：
 * 这是一个AOP（面向切面编程）切面类，用于处理与Redis缓存操作相关的异常。
 * 它通过环绕通知（@Around）拦截特定缓存服务方法的执行。
 * 主要目的是：
 * 1. 统一处理缓存操作中可能发生的异常。
 * 2. 根据被拦截方法是否使用了 {@link CacheException} 注解，来决定异常处理策略：
 *    - 如果方法使用了 {@link CacheException} 注解，则当该方法抛出异常时，此切面会将异常原样向上抛出，允许调用者处理。
 *    - 如果方法没有使用 {@link CacheException} 注解，则当该方法抛出异常时，此切面会捕获异常，记录错误日志，但不会将异常向上抛出（即“吞掉”异常），
 *      这样可以防止因缓存服务（如Redis）暂时不可用等问题导致整个业务流程中断，实现一种缓存操作的容错或优雅降级。
 * {@link Order @Order(2)} 注解指定了当有多个切面作用于同一个连接点（join point）时，此切面的执行顺序。数值越小，优先级越高。
 */
@Aspect // (3) @Aspect: 声明这是一个切面类，Spring AOP会识别并处理它。
@Component // (3) @Component: 将这个切面类注册为Spring容器中的一个Bean，以便Spring能够管理它并应用其定义的切面逻辑。
@Order(2) // (3) @Order(2): 定义了此切面在多个切面共同作用于同一个连接点时的执行顺序。数字越小，优先级越高。
public class RedisCacheAspect {
    // (3) 获取一个日志记录器实例，用于记录日志。日志记录器的名称与当前类（RedisCacheAspect）相关联。
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisCacheAspect.class);

    /**
     * (2) 方法的整体作用说明：
     * 定义了一个切点（Pointcut），名为 "cacheAspect"。
     * 切点表达式指定了此切面将要拦截的目标方法范围。
     * 具体来说，它匹配了：
     * 1. `com.lzy.mall.portal.service` 包下所有类名以 `CacheService` 结尾的类中的所有公共方法。
     * 2. `com.lzy.mall.service` 包下所有类名以 `CacheService` 结尾的类中的所有公共方法。
     * 这个方法本身不包含任何执行逻辑，它仅仅是作为一个命名的切点，供下面的 {@link Around @Around} 通知引用。
     */
    @Pointcut("execution(public * com.lzy.mall.portal.service.*CacheService.*(..)) || execution(public * com.lzy.mall.service.*CacheService.*(..))")
    public void cacheAspect() {
        // (3) 此方法体为空，因为它仅用于定义切点表达式，不执行实际操作。
    }

    /**
     * (2) 方法的整体作用说明：
     * 这是一个环绕通知（Around Advice）方法，它作用于上面定义的 "cacheAspect" 切点所匹配到的方法。
     * 环绕通知可以在目标方法执行之前和之后执行自定义逻辑，并且可以控制目标方法是否执行、如何执行，甚至修改其返回值或抛出异常。
     * 此方法的核心逻辑是：
     * 1. 执行目标方法（通过 `joinPoint.proceed()`）。
     * 2. 捕获目标方法执行过程中可能抛出的任何异常（`Throwable`）。
     * 3. 检查目标方法上是否存在 {@link CacheException} 注解。
     *    - 如果存在，则将捕获到的异常重新抛出。
     *    - 如果不存在，则记录错误日志，并且不抛出异常（异常被“吞掉”）。
     * 4. 返回目标方法的执行结果。
     *
     * @param joinPoint 包含连接点（即被拦截的方法）的上下文信息，如方法签名、参数等，并提供了执行目标方法的 `proceed()` 方法。
     * @return 目标方法的执行结果，如果在执行过程中发生异常且该异常被“吞掉”，则可能返回 `null` 或方法执行到异常发生前的部分结果（取决于具体情况）。
     * @throws Throwable 如果目标方法本身抛出异常，并且该方法被 {@link CacheException} 注解标记，则此通知方法会原样抛出该异常。
     */
    @Around("cacheAspect()") // (3) @Around("cacheAspect()"): 指定这是一个环绕通知，它将应用于名为 "cacheAspect" 的切点。
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        // (3) 获取被拦截方法的签名信息。签名包含了方法的名称、参数类型、返回类型等。
        Signature signature = joinPoint.getSignature();
        // (3) 将通用的 Signature 对象转换为更具体的 MethodSignature 对象，以便能获取到 Method 对象。
        MethodSignature methodSignature = (MethodSignature) signature;
        // (3) 从 MethodSignature 中获取被拦截的实际 Method 对象。这对于后续检查方法上的注解至关重要。
        Method method = methodSignature.getMethod();
        // (3) 初始化一个变量用于存储目标方法的执行结果。
        Object result = null;
        try {
            // (3) 调用 `joinPoint.proceed()` 来执行被拦截的目标方法。
            // (3) 如果目标方法成功执行，其返回值会被赋给 `result`。
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            // (3) 捕获目标方法执行过程中抛出的任何类型的异常或错误。
            // (3) 检查被拦截的 Method 对象上是否存在 CacheException 类型的注解。
            if (method.isAnnotationPresent(CacheException.class)) {
                // (3) 如果方法上存在 @CacheException 注解，则将捕获到的异常 `throwable` 原样向上抛出。
                // (3) 这意味着，对于标记了 @CacheException 的缓存方法，其异常会正常传播给调用者。
                throw throwable;
            } else {
                // (3) 如果方法上没有 @CacheException 注解，则表示我们希望优雅地处理这个缓存异常。
                // (3) 使用 SLF4J Logger 记录错误级别的信息，内容为异常的消息。
                // (3) 这样做可以防止因缓存服务问题（如Redis连接失败）导致整个业务流程失败，
                // (3) 而是仅仅记录错误，业务流程可能会继续（可能获取到的是默认值或null，取决于后续业务逻辑）。
                LOGGER.error(throwable.getMessage());
            }
        }
        // (3) 返回目标方法的执行结果。
        // (3) 如果目标方法正常执行，则返回其真实结果。
        // (3) 如果目标方法抛出异常但该异常被此切面“吞掉”（即未标记 @CacheException），
        // (3) 那么 `result` 可能是 `null` (如果它在 `proceed()` 之前初始化为 `null` 且 `proceed()` 抛异常)
        // (3) 或者是在 `proceed()` 中成功赋值但在后续代码（如果有）中发生异常（此例中不太可能）。
        return result;
    }

}