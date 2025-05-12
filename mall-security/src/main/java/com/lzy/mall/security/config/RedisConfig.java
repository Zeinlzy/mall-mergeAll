package com.lzy.mall.security.config;

import com.lzy.mall.common.config.BaseRedisConfig;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Redis 配置类
 * 这是一个Spring配置类，用于配置和启用基于Redis的缓存功能。
 * 它继承自 BaseRedisConfig，这意味着它会复用 BaseRedisConfig 中定义的基础Redis连接、序列化等配置 Bean (例如 RedisTemplate)。
 * 通过 @EnableCaching 注解，此类启用了 Spring 的声明式缓存支持，
 * 使得可以在服务方法上使用 @Cacheable, @CachePut, @CacheEvict 等注解，并使用 Redis 作为实际的缓存存储。
 * 此类自身没有定义具体的Redis连接或操作Bean，这些由其父类 BaseRedisConfig 提供。
 */
@EnableCaching // (1) 类注释的一部分：启用Spring的基于注解的缓存支持
@Configuration // (1) 类注释的一部分：将此类标记为Spring配置类
public class RedisConfig extends BaseRedisConfig {

    // (2) 方法注释：
    // 注意：此类自身没有定义任何新的 @Bean 方法或其它方法。
    // 它主要通过继承父类 BaseRedisConfig 来获取基础的Redis配置Bean (如 RedisTemplate)，
    // 并通过类上的 @EnableCaching 注解来启用Redis缓存功能。
    // 因此，在这个特定的 RedisConfig 类中，没有需要单独注释的方法或方法体内的代码。
    // 所有具体的Redis配置Bean定义和逻辑都在 BaseRedisConfig 中。

    // (3) 方法体代码注释：
    // 由于此类没有定义任何方法，因此也没有方法体内的代码需要注释。
    // 如果 BaseRedisConfig 中有方法，注释应该写在那里。
}