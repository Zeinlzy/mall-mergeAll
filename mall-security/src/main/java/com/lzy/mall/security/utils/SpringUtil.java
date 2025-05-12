package com.lzy.mall.security.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * (1) 类的整体作用说明：
 * SpringUtil 是一个工具类，用于在 Spring IoC 容器环境之外（例如，在一些非 Spring 管理的类中，或者在静态方法中）
 * 方便地获取 Spring 容器中管理的 Bean 实例。
 * 它通过实现 {@link ApplicationContextAware} 接口，在 Spring 容器初始化时自动获取并持有
 * {@link ApplicationContext}（Spring 应用上下文）的引用。
 * 这样，其他代码就可以通过此类提供的静态方法来访问 Spring 容器中的 Bean。
 *
 * 使用场景：
 * - 当一个非 Spring 管理的类需要调用 Spring 管理的 Bean 的服务时。
 * - 在静态方法中需要获取 Spring Bean 时。
 * - 在某些框架集成或特殊场景下，无法直接通过依赖注入获取 Bean 时。
 *
 * 注意：过度依赖此类可能会破坏 Spring 的依赖注入原则，应谨慎使用。
 * 优先考虑使用标准的依赖注入方式（如 @Autowired, 构造函数注入等）。
 */
@Component // (3) @Component: 将 SpringUtil 类注册为 Spring IoC 容器中的一个 Bean。这使得 Spring 容器能够管理它，并回调其 ApplicationContextAware 接口的方法。
public class SpringUtil implements ApplicationContextAware {

    // (3) 定义一个静态变量来持有 Spring 的 ApplicationContext。
    // (3) 静态变量使得在整个应用程序的生命周期内，都可以通过此类访问到同一个 ApplicationContext 实例。
    private static ApplicationContext applicationContext;

    /**
     * (2) 方法的整体作用说明：
     * 获取静态持有的 Spring {@link ApplicationContext} 实例。
     * 这是其他 `getBean` 方法获取 Bean 的基础。
     *
     * @return 返回应用程序上下文 {@link ApplicationContext}。如果 Spring 容器尚未初始化完成或者此类未被正确配置，可能返回 `null`。
     */
    public static ApplicationContext getApplicationContext() {
        // (3) 返回存储的 applicationContext 实例。
        return applicationContext;
    }

    /**
     * (2) 方法的整体作用说明：
     * 这是 {@link ApplicationContextAware} 接口要求实现的方法。
     * 当 Spring 容器创建并初始化此类（SpringUtil Bean）时，会自动调用此方法，
     * 并将当前的 {@link ApplicationContext} 实例作为参数传入。
     * 此方法用于将获取到的 ApplicationContext 存储到静态变量中，以便后续使用。
     *
     * @param applicationContext Spring 应用程序上下文实例。
     * @throws BeansException 如果在设置应用程序上下文时发生错误。
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // (3) 检查静态的 applicationContext 是否已经被赋值。
        // (3) 这样做的目的是防止 applicationContext 被意外地多次覆盖，尽管在正常的 Spring 生命周期中，setApplicationContext 通常只会被调用一次。
        if (SpringUtil.applicationContext == null) {
            // (3) 如果静态的 applicationContext 为 null (即首次初始化)，则将其赋值为传入的 applicationContext。
            SpringUtil.applicationContext = applicationContext;
        }
    }

    /**
     * (2) 方法的整体作用说明：
     * 根据 Bean 的名称（在 Spring 容器中定义的 id 或 name）从 ApplicationContext 中获取 Bean 实例。
     *
     * @param name Bean 在 Spring 容器中的名称。
     * @return 返回查找到的 Bean 实例，类型为 Object。调用者需要进行类型转换。
     *         如果找不到指定名称的 Bean，会抛出 {@link org.springframework.beans.factory.NoSuchBeanDefinitionException}。
     */
    public static Object getBean(String name) {
        // (3) 调用 getApplicationContext() 获取 Spring 应用上下文，然后通过上下文的 getBean(name) 方法获取 Bean。
        return getApplicationContext().getBean(name);
    }

    /**
     * (2) 方法的整体作用说明：
     * 根据 Bean 的 Class 类型从 ApplicationContext 中获取 Bean 实例。
     * 这种方式适用于当容器中只有一个该类型的 Bean，或者明确知道需要哪个 primary Bean 的情况。
     *
     * @param clazz Bean 的 Class 对象。
     * @param <T>   Bean 的泛型类型。
     * @return 返回查找到的 Bean 实例，类型为指定的 Class 类型 T。
     *         如果找不到该类型的 Bean，或找到多个该类型的 Bean 且没有一个被标记为 primary，会抛出相应的异常
     *         (如 {@link org.springframework.beans.factory.NoSuchBeanDefinitionException} 或 {@link org.springframework.beans.factory.NoUniqueBeanDefinitionException})。
     */
    public static <T> T getBean(Class<T> clazz) {
        // (3) 调用 getApplicationContext() 获取 Spring 应用上下文，然后通过上下文的 getBean(clazz) 方法获取 Bean。
        return getApplicationContext().getBean(clazz);
    }

    /**
     * (2) 方法的整体作用说明：
     * 根据 Bean 的名称和 Class 类型从 ApplicationContext 中获取 Bean 实例。
     * 这种方式既指定了 Bean 的名称，也指定了期望的类型，提供了更精确的 Bean 获取方式，并避免了手动类型转换。
     *
     * @param name  Bean 在 Spring 容器中的名称。
     * @param clazz Bean 的 Class 对象。
     * @param <T>   Bean 的泛型类型。
     * @return 返回查找到的 Bean 实例，类型为指定的 Class 类型 T。
     *         如果找不到指定名称和类型的 Bean，会抛出 {@link org.springframework.beans.factory.NoSuchBeanDefinitionException}。
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        // (3) 调用 getApplicationContext() 获取 Spring 应用上下文，然后通过上下文的 getBean(name, clazz) 方法获取 Bean。
        return getApplicationContext().getBean(name, clazz);
    }

}