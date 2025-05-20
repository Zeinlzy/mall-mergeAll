package com.lzy.mall.common.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties; // 导入这个注解
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * Swagger自定义配置属性类。
 * 这个类用于封装从Spring Boot配置文件 (如 application.yml 或 application.properties) 中加载的与Swagger (OpenAPI) 文档生成相关的配置项。
 */
@Configuration
@Data // Lombok: 自动生成getter, setter, toString, equals, hashCode等方法
@EqualsAndHashCode // Lombok: 自动生成equals和hashCode方法 (被@Data包含，可省略)
@ConfigurationProperties(prefix = "swagger") // Spring Boot注解: 标记此类为配置属性类，
// 并指定从配置文件中读取以 "swagger" 为前缀的属性
public class SwaggerProperties {
    /**
     * API文档生成基础路径。
     * 指定SpringDoc应该扫描哪个包下的Controller来生成API接口文档。
     * 示例配置: swagger.api-base-package=com.lzy.mall.controller
     */
    private String apiBasePackage;

    /**
     * 是否启用登录认证。
     * 如果为true，Swagger文档将显示安全方案配置，允许用户输入如Token等认证信息。
     * 默认为false。
     * 示例配置: swagger.enable-security=true
     */
    private boolean enableSecurity = true; // 默认不启用安全特性

    /**
     * API文档的标题。
     * 显示在Swagger UI页面的顶部。
     * 默认为 "API Documentation"。
     * 示例配置: swagger.title=LZY商城API
     */
    private String title = "API Documentation"; // 默认标题

    /**
     * API文档的描述信息。
     * 对API进行更详细的说明。
     * 默认为 "RESTful API Documentation"。
     * 示例配置: swagger.description=LZY商城后端服务接口文档
     */
    private String description = "RESTful API Documentation"; // 默认描述

    /**
     * API文档的版本号。
     * 默认为 "1.0"。
     * 示例配置: swagger.version=v1.0
     */
    private String version = "1.0"; // 默认版本

    /**
     * API文档中显示的联系人姓名。
     * 默认为 "Developer"。
     * 示例配置: swagger.contact-name=技术支持团队
     */
    private String contactName = "Developer"; // 默认联系人名称

    /**
     * API文档中显示的联系人网址。
     * 默认为空字符串。
     * 示例配置: swagger.contact-url=http://www.example.com
     */
    private String contactUrl = ""; // 默认联系人URL

    /**
     * API文档中显示的联系人邮箱。
     * 默认为空字符串。
     * 示例配置: swagger.contact-email=support@example.com
     */
    private String contactEmail = ""; // 默认联系人邮箱
}