package com.lzy.mall.common.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * WebLog 类是一个数据封装类 (Data Transfer Object - DTO 或 Plain Old Java Object - POJO)，专门用于收集和承载 Web 请求处理过程中的各种详细信息
 * Controller层Web请求的日志封装类。
 * 该类用于收集和承载HTTP请求处理过程中的详细信息，
 * 以便进行结构化的日志记录和后续分析。
 * 通常由AOP切面在Controller方法执行前后填充数据。
 */
@Data // Lombok: 自动生成getter, setter, toString, equals, hashCode等方法
@EqualsAndHashCode // Lombok: 自动生成equals和hashCode方法 (被@Data包含，可省略，除非有特定配置需求)
public class WebLog {
    /**
     * 操作描述。
     * 对当前Web请求所执行业务操作的简短描述，例如："用户登录"、"获取商品详情"。
     */
    private String description;

    /**
     * 操作用户。
     * 执行该操作的用户的身份标识，例如用户名。通常从安全上下文中获取。
     */
    private String username;

    /**
     * 操作开始时间。
     * 请求处理开始的精确时间戳 (通常是System.currentTimeMillis())。
     */
    private Long startTime;

    /**
     * 操作消耗时间 (单位：毫秒)。
     * 处理该请求所花费的总时间。
     */
    private Integer spendTime;

    /**
     * 基础路径 (根路径)。
     * 例如 ServletContextPath，或者请求URL的主机和端口之后应用的基本路径部分。
     * (例如：对于 'http://localhost:8080/myapp/api/users', basePath可能是 '/myapp' 或 'http://localhost:8080/myapp')
     */
    private String basePath;

    /**
     * 请求的URI (Uniform Resource Identifier)。
     * URL中服务器地址之后、查询参数之前的部分。
     * (例如：对于 'http://localhost:8080/myapp/api/users?id=1', URI是 '/myapp/api/users')
     */
    private String uri;

    /**
     * 完整的请求URL (Uniform Resource Locator)。
     * (例如：'http://localhost:8080/myapp/api/users?id=1')
     */
    private String url;

    /**
     * HTTP请求方法。
     * 例如：GET, POST, PUT, DELETE 等。
     */
    private String method;

    /**
     * 请求来源的IP地址。
     * 发起该HTTP请求的客户端的IP地址。
     */
    private String ip;

    /**
     * 请求参数。
     * HTTP请求中携带的参数，可以是查询参数、路径变量或请求体。
     * 由于类型不固定，使用Object类型存储，具体内容可能是Map、JSON字符串或请求DTO对象。
     */
    private Object parameter;

    /**
     * 请求处理结果。
     * Controller方法执行后返回的数据。
     * 由于类型不固定，使用Object类型存储。
     */
    private Object result;

}
