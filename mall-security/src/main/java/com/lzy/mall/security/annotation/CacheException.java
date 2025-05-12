package com.lzy.mall.security.annotation;

import java.lang.annotation.*;

/**
 * (1) 类的整体作用说明：
 * 自定义注解 @CacheException。
 * 这个注解通常用于标记那些在执行缓存操作（如读取、写入、删除缓存）时可能抛出异常的方法。
 * 其主要目的是为了让AOP（面向切面编程）能够识别这些方法，并对它们可能抛出的异常进行统一处理，
 * 例如记录日志、返回默认值、或者确保即使缓存服务出现问题，主业务流程也不会被中断，实现优雅降级。
 * 它本身不包含任何处理逻辑，只是一个标记，具体的异常处理逻辑由切面或其他机制实现。
 */
@Documented // (3) @Documented: 元注解，表示这个注解应该被 javadoc 工具记录。即，如果一个类或方法使用了 @CacheException 注解，那么在生成的 Javadoc 中会显示这个注解。
@Target(ElementType.METHOD) // (3) @Target(ElementType.METHOD): 元注解，指定了这个注解可以应用的目标元素类型。ElementType.METHOD 表示 @CacheException 只能用于修饰方法。
@Retention(RetentionPolicy.RUNTIME) // (3) @Retention(RetentionPolicy.RUNTIME): 元注解，指定了注解的保留策略。RetentionPolicy.RUNTIME 表示这个注解会被保留到运行时，JVM 将在运行时保留此注解，因此可以通过反射机制读取和使用它。这对于AOP等需要在运行时检查注解并执行相应逻辑的场景至关重要。
public @interface CacheException {
    // (2) 方法的整体作用说明（对于注解而言，这里指注解本身的用途和特性）：
    // 此注解 @CacheException 定义了一个标记。
    // 当一个方法被此注解标记时，它暗示了这个方法内部的缓存相关操作可能会失败（例如，连接Redis超时、Redis服务不可用等）。
    // 通常，这意味着开发者期望通过某种机制（如AOP切面）来捕获并处理这些潜在的异常，
    // 以避免因为缓存系统的暂时性问题而影响到核心业务逻辑的执行，或者为了实现特定的错误处理策略（如重试、降级等）。
    // 该注解本身没有属性，仅作为一种元数据标记存在。
}