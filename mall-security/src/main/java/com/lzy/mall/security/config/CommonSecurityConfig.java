package com.lzy.mall.security.config;

import com.lzy.mall.security.component.*;
import com.lzy.mall.security.utils.JwtTokenUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


/**
 * 通用安全配置类
 * 这是一个Spring配置类，用于配置与Spring Security相关的通用Bean，
 * 例如密码编码器、JWT工具类、访问拒绝和未授权处理类，以及动态权限相关的组件等。
 * 这些Bean可以在应用程序的其他地方注入和使用。
 */
@Configuration
public class CommonSecurityConfig {

    /**
     * 配置密码编码器Bean
     * 提供一个BCryptPasswordEncoder实例作为Spring Bean，
     * 用于对用户密码进行加密和验证。
     * @return BCryptPasswordEncoder实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 创建并返回一个使用BCrypt算法的密码编码器
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置需要忽略安全控制的URL路径的Bean
     * 提供一个IgnoreUrlsConfig实例作为Spring Bean，
     * 该实例通常包含不需要进行Spring Security拦截的公共访问路径。
     * @return IgnoreUrlsConfig实例
     */
    @Bean
    public IgnoreUrlsConfig ignoreUrlsConfig() {
        // 创建并返回一个IgnoreUrlsConfig实例
        return new IgnoreUrlsConfig();
    }

    /**
     * 配置用于生成、验证和解析JWT令牌的工具类Bean
     * 提供一个JwtTokenUtil实例作为Spring Bean，
     * 用于处理基于JWT的认证逻辑。
     * @return JwtTokenUtil实例
     */
    @Bean
    public JwtTokenUtil jwtTokenUtil() {
        // 创建并返回一个JwtTokenUtil实例
        return new JwtTokenUtil();
    }

    /**
     * 配置处理未授权访问（已认证但无权限）的Handler Bean
     * 提供一个RestfulAccessDeniedHandler实例作为Spring Bean，
     * 当已认证的用户试图访问他们没有权限的资源时，会调用此Handler。
     * 通常用于返回一个REST风格的错误响应。
     * @return RestfulAccessDeniedHandler实例
     */
    @Bean
    public RestfulAccessDeniedHandler restfulAccessDeniedHandler() {
        // 创建并返回一个RestfulAccessDeniedHandler实例
        return new RestfulAccessDeniedHandler();
    }

    /**
     * 配置处理未认证请求（未登录访问受保护资源）的EntryPoint Bean
     * 提供一个RestAuthenticationEntryPoint实例作为Spring Bean，
     * 当未认证的用户试图访问受保护的资源时，会调用此EntryPoint。
     * 通常用于返回一个REST风格的错误响应，指示需要认证。
     * @return RestAuthenticationEntryPoint实例
     */
    @Bean
    public RestAuthenticationEntryPoint restAuthenticationEntryPoint() {
        // 创建并返回一个RestAuthenticationEntryPoint实例
        return new RestAuthenticationEntryPoint();
    }

    /**
     * 配置处理JWT认证令牌的过滤器Bean
     * 提供一个JwtAuthenticationTokenFilter实例作为Spring Bean，
     * 该过滤器通常用于从HTTP请求中解析JWT令牌，并设置Spring Security的认证上下文。
     * @return JwtAuthenticationTokenFilter实例
     */
    @Bean
    public JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter(){
        // 创建并返回一个JwtAuthenticationTokenFilter实例
        return new JwtAuthenticationTokenFilter();
    }

    /**
     * 配置动态权限决策管理器Bean
     * 提供一个DynamicAccessDecisionManager实例作为Spring Bean。
     * 这是Spring Security授权过程中的一部分，用于根据配置的规则决定是否允许访问资源。
     * @ConditionalOnBean(name = "dynamicSecurityService")：表示只有当Spring容器中存在名为"dynamicSecurityService"的Bean时，才会创建此Bean。
     * @return DynamicAccessDecisionManager实例
     */
    @ConditionalOnBean(name = "dynamicSecurityService")
    @Bean
    public DynamicAccessDecisionManager dynamicAccessDecisionManager() {
        // 创建并返回一个DynamicAccessDecisionManager实例
        return new DynamicAccessDecisionManager();
    }

    /**
     * 配置动态安全元数据源Bean
     * 提供一个DynamicSecurityMetadataSource实例作为Spring Bean。
     * 此组件用于加载资源的访问规则（例如，哪些URL需要哪些权限）。
     * "Dynamic"表示这些规则可以动态加载（例如从数据库）。
     * @ConditionalOnBean(name = "dynamicSecurityService")：表示只有当Spring容器中存在名为"dynamicSecurityService"的Bean时，才会创建此Bean。
     * @return DynamicSecurityMetadataSource实例
     */
    @ConditionalOnBean(name = "dynamicSecurityService")
    @Bean
    public DynamicSecurityMetadataSource dynamicSecurityMetadataSource() {
        // 创建并返回一个DynamicSecurityMetadataSource实例
        return new DynamicSecurityMetadataSource();
    }

    /**
     * 配置动态安全过滤器Bean
     * 提供一个DynamicSecurityFilter实例作为Spring Bean。
     * 该过滤器通常用于将动态权限控制逻辑集成到Spring Security的过滤链中，
     * 在认证之后，进行基于动态规则的授权检查。
     * @ConditionalOnBean(name = "dynamicSecurityService")：表示只有当Spring容器中存在名为"dynamicSecurityService"的Bean时，才会创建此Bean。
     * @return DynamicSecurityFilter实例
     */
    @ConditionalOnBean(name = "dynamicSecurityService")
    @Bean
    public DynamicSecurityFilter dynamicSecurityFilter(){
        // 创建并返回一个DynamicSecurityFilter实例
        return new DynamicSecurityFilter();
    }
}