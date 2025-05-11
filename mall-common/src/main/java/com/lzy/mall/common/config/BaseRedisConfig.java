package com.lzy.mall.common.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
//import com.lzy.mall.common.service.RedisService;
//import com.lzy.mall.common.service.impl.RedisServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Spring redis 配置类，用来创建和配置几个与 Redis 相关的 核心 Bean (组件)，并将它们注册到 Spring 的应用上下文中。
 * 这样，项目中的其他部分就可以通过依赖注入（例如使用 @Autowired）来获取并使用这些配置好的 Redis 组件，而不需要自己去手动创建和配置它们。
 */
public class BaseRedisConfig {

    /**
     *RedisTemplate<String, Object>: 这是 Spring Data Redis 提供的用于直接操作 Redis 数据结构（String, List, Set, Hash, Sorted Set 等）的核心客户端工具。
     * 这段代码配置了它的连接工厂和各种数据（Key, Value, Hash Key, Hash Value）的序列化方式。
     */
    @Bean  // 声明这是一个 Spring Bean。Spring 容器会执行这个方法，并将返回的对象注册为一个可管理的 Bean。
    public RedisTemplate<String, Object> redisTemplate(
            // Spring 会自动从容器中查找一个 RedisConnectionFactory 类型的 Bean 并注入到这里。
            // RedisConnectionFactory 负责建立和管理与 Redis 服务器的连接。
            RedisConnectionFactory redisConnectionFactory,
            // Spring 会自动从容器中查找一个 RedisSerializer<Object> 类型的 Bean 并注入到这里。
            // RedisSerializer 负责 Java 对象与字节数组之间的序列化和反序列化，以便存储和读取 Redis 数据。
            // 在这个配置中，通常会注入一个用于JSON序列化的Serializer，例如Jackson2JsonRedisSerializer。
            RedisSerializer<Object> redisSerializer) {

        // 创建一个 RedisTemplate 实例。这是 Spring Data Redis 提供的核心类，用于执行各种 Redis 操作。
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

        // 设置 Redis 的连接工厂。通过这个连接工厂，RedisTemplate 才能与实际的 Redis 服务器进行通信。
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 设置 key（键）的序列化器。这里使用 StringRedisSerializer，确保所有的 key 都以字符串形式存储在 Redis 中。
        // 这样在 Redis 客户端（如 redis-cli）中查看 key 时是可读的字符串。
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        // 设置 value（值）的序列化器。这里使用注入的 redisSerializer，通常配置为处理复杂的 Java 对象（如 POJO）。
        // 例如，如果注入的是 Jackson2JsonRedisSerializer，值就会被序列化成 JSON 字符串存储。
        redisTemplate.setValueSerializer(redisSerializer);

        // 设置 hash key（哈希结构中的字段名）的序列化器。同样使用 StringRedisSerializer，确保字段名是字符串。
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        // 设置 hash value（哈希结构中的字段值）的序列化器。同样使用注入的 redisSerializer，用于序列化存储在哈希中的值。
        redisTemplate.setHashValueSerializer(redisSerializer);

        // 调用 afterPropertiesSet() 方法，执行 RedisTemplate 的初始化逻辑。
        // 这个方法会检查所有必要的属性（如连接工厂和序列化器）是否已经设置，并进行一些内部设置。
        redisTemplate.afterPropertiesSet();

        // 将配置好的 RedisTemplate 实例返回，Spring 容器会将其注册为一个 Bean。
        return redisTemplate;
    }

    /**
     * RedisSerializer<Object>: 这是一个用于将 Java 对象序列化成适合存储在 Redis 的字节格式（以及反序列化）的 Bean。
     * 这里的实现使用了 Jackson 库，这意味着 Java 对象会被序列化成 JSON 格式 存储在 Redis 中，并且配置了处理复杂类型（如集合、多态对象）的反序列化问题。
     */
    @Bean  // 声明这是一个 Spring Bean。这个方法将创建一个用于 Redis 值序列化的 Bean。
    public RedisSerializer<Object> redisSerializer() {
        // 创建 Jackson 的 ObjectMapper 实例。ObjectMapper 是 Jackson 库中用于 JSON(文本) 和 Java 对象之间转换的核心类。
        ObjectMapper objectMapper = new ObjectMapper();

        // 配置 ObjectMapper，使其能够访问 Java 对象的任何可见性（public, private 等）的字段或属性。
        // 这通常是为了确保即使是私有字段也能被序列化和反序列化。
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        // 启用 Jackson 的默认类型信息。这是关键一步，用于处理多态性（Polymorphism）。
        // 当存储的是一个接口或父类类型的变量，但实际运行时是一个子类对象时，
        // 这个设置会在 JSON 中加入类型信息（@class 属性），以便反序列化时 Jackson 知道应该创建哪个具体的子类对象。
        // LaissezFaireSubTypeValidator.instance 是一个允许大多数子类型的验证器。
        // ObjectMapper.DefaultTyping.NON_FINAL 指示只为非 final 类型的对象添加类型信息。
        // 这个设置对于正确反序列化复杂对象（如集合、包含接口字段的对象）非常重要，
        // 否则 Jackson 在反序列化时可能不知道具体类型，默认会解析成 Map 等通用类型，导致类型转换错误。
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,ObjectMapper.DefaultTyping.NON_FINAL);

        // 创建 Jackson2JsonRedisSerializer 实例。这是 Spring Data Redis 提供的一个 RedisSerializer 实现，它底层使用 Jackson 进行 JSON 序列化。
        // 将上面配置好的 objectMapper 传递给构造方法，确保序列化器使用我们自定义的配置。
        // Object.class 表示这个序列化器可以处理任何 Object 类型的对象。
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);

        // 将创建和配置好的 Jackson2JsonRedisSerializer 实例返回。Spring 容器会将其注册为一个名为 "redisSerializer" 的 Bean。
        // 这个 Bean 将被上面的 redisTemplate 方法注入并使用，用于序列化/反序列化 Redis 中的值和哈希字段值。
        return serializer;
    }

    /**
     *RedisCacheManager: 这是 Spring Cache 抽象层与 Redis 集成的关键。
     * 通过配置这个 Bean，你可以使用 Spring 提供的缓存注解（如 @Cacheable, @CachePut, @CacheEvict）来方便地将方法结果缓存到 Redis 中。
     * 它使用了上面定义的 redisSerializer 来处理缓存值的序列化，并设置了默认的缓存过期时间（1天）。
     */
    // 声明这是一个 Spring Bean。这个方法将创建一个用于 Spring 缓存抽象的 Redis 缓存管理器 Bean。
    // RedisCacheManager 是 Spring Cache 的核心组件，它使得你可以使用 @Cacheable 等注解来集成 Redis 作为缓存。
    @Bean
    public RedisCacheManager redisCacheManager(
            // Spring 会自动从容器中查找一个 RedisConnectionFactory 类型的 Bean 并注入。
            // 这是缓存管理器连接到 Redis 服务器的基础。
            RedisConnectionFactory redisConnectionFactory,
            // Spring 会自动从容器中查找一个 RedisSerializer<Object> 类型的 Bean 并注入。
            // 这个序列化器将用于缓存 Value 的序列化和反序列化（即存储在 Redis 中的数据内容）。
            RedisSerializer<Object> redisSerializer) {

        // 创建一个 RedisCacheWriter。它是 RedisCacheManager 的底层组件，负责直接与 Redis 进行读写交互。
        // nonLockingRedisCacheWriter 表示在写入缓存时不会使用锁，这通常能提供更好的性能。
        RedisCacheWriter redisCacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory);

        // 创建一个 RedisCacheConfiguration 对象，用于定义缓存的行为设置。
        // defaultCacheConfig() 获取一个默认的缓存配置。
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                // 设置 Value 的序列化器。使用注入的 redisSerializer 来处理缓存值的存储和读取。
                // RedisSerializationContext.SerializationPair.fromSerializer 将同一个序列化器用于序列化和反序列化。
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer))
                // 设置缓存项的默认过期时间 (TTL - Time To Live)。这里设置为 1 天。
                // 缓存项到达这个时间后会自动失效并从 Redis 中移除。
                .entryTtl(Duration.ofDays(1));

        // 使用创建好的 RedisCacheWriter 和 RedisCacheConfiguration 构建并返回 RedisCacheManager 实例。
        // Spring 容器将这个实例注册为名为 "redisCacheManager" 的 Bean。
        // 应用程序中的 Spring Cache 相关注解（如 @Cacheable）将使用这个管理器来操作 Redis 缓存。
        return new RedisCacheManager(redisCacheWriter, redisCacheConfiguration);
    }

//    @Bean
//    public RedisService redisService(){
//        return new RedisServiceImpl();
//    }

}