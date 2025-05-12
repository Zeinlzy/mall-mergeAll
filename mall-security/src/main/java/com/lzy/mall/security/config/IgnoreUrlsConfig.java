package com.lzy.mall.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 需要忽略安全控制的URL路径配置类
 * 这是一个用于存储在Spring Security中不需要进行安全拦截（即忽略）的URL路径列表的配置类。
 * 通过使用@ConfigurationProperties注解，它可以自动从Spring Boot的配置文件（如application.yml或application.properties）
 * 中读取以 "secure.ignored" 为前缀的属性值，并将它们绑定到此类中的字段上。
 * 通常用于定义公共访问路径，例如登录接口、静态资源等。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "secure.ignored") // Spring Boot注解，指定从配置文件中读取前缀为 "secure.ignored" 的属性，并绑定到此类字段。
public class IgnoreUrlsConfig {

    /**
     * 需要忽略安全控制的URL路径列表
     * 此字段用于存储从配置文件中读取的，不需要被Spring Security拦截的URL路径。
     * 例如，在application.yml中配置 secure.ignored.urls: ["/login", "/register", "/statics/**"]，这些路径就会被加载到此列表中。
     */
    private List<String> urls = new ArrayList<>(); // 声明一个私有的字符串列表，用于存储忽略的URL路径，并初始化为空ArrayList。

    // Lombok的@Getter和@Setter注解会自动生成以下方法（无需手动编写）：

    /**
     * 获取需要忽略安全控制的URL路径列表
     * 由@Getter注解自动生成。
     * @return 忽略的URL路径列表
     */
    // public List<String> getUrls() { return urls; }

    /**
     * 设置需要忽略安全控制的URL路径列表
     * 由@Setter注解自动生成。Spring Boot在属性绑定时会调用此方法。
     * @param urls 需要忽略的URL路径列表
     */
    // public void setUrls(List<String> urls) { this.urls = urls; }
}
