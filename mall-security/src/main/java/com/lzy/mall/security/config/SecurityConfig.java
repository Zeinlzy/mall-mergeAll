package com.lzy.mall.security.config;

// 导入可能需要的Spring Security相关的类
import com.lzy.mall.security.component.DynamicSecurityFilter;
import com.lzy.mall.security.component.DynamicSecurityService;
import com.lzy.mall.security.component.JwtAuthenticationTokenFilter;
import com.lzy.mall.security.component.RestAuthenticationEntryPoint;
import com.lzy.mall.security.component.RestfulAccessDeniedHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // 导入HttpMethod类
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy; // 导入SessionCreationPolicy类
import org.springframework.security.web.SecurityFilterChain; // 导入SecurityFilterChain类
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor; // 导入FilterSecurityInterceptor类
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // 导入UsernamePasswordAuthenticationFilter类

/**
 * Spring Security 配置类
 * 这是一个Spring配置类，用于定义整个Web应用程序的Spring Security安全配置链。
 * 它配置了哪些URL需要保护，哪些允许公共访问，如何处理认证和授权失败，
 * 以及如何集成JWT过滤器和动态权限过滤器等。
 * 使用@EnableWebSecurity注解启用Spring Security的Web安全功能。
 */
@Configuration
@EnableWebSecurity // 启用Spring Security的Web安全功能
public class SecurityConfig {

    // 注入忽略安全控制的URL配置Bean
    @Autowired
    private IgnoreUrlsConfig ignoreUrlsConfig;
    // 注入处理无权访问（已认证但无权限）的Handler Bean
    @Autowired
    private RestfulAccessDeniedHandler restfulAccessDeniedHandler;
    // 注入处理未认证请求（未登录访问）的EntryPoint Bean
    @Autowired
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    // 注入处理JWT认证令牌的过滤器 Bean
    @Autowired
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;
    // 注入动态权限服务 Bean (可选，因为使用了required = false)
    @Autowired(required = false)
    private DynamicSecurityService dynamicSecurityService;
    // 注入动态权限过滤器 Bean (可选，因为使用了required = false)
    @Autowired(required = false)
    private DynamicSecurityFilter dynamicSecurityFilter;

    /**
     * 配置安全过滤器链
     * 定义了Spring Security如何处理HTTP请求的安全规则。
     * 使用HttpSecurity对象进行链式配置，包括CSRF禁用、Session管理策略、
     * 异常处理、URL访问权限控制、以及添加自定义过滤器。
     * @param httpSecurity HttpSecurity配置对象
     * @return 配置好的SecurityFilterChain
     * @throws Exception 配置过程中可能抛出的异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity
                //配置CSRF防护：禁用CSRF功能，适用于无状态的RESTful API
                .csrf(csrf -> csrf.disable())

                //配置Session管理：设置为无状态，不创建或使用HTTP Session，通常用于基于Token的认证
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                //配置异常处理：自定义认证失败和权限不足时的处理逻辑
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(restfulAccessDeniedHandler) // 配置已认证用户访问无权限资源时的Handler
                        .authenticationEntryPoint(restAuthenticationEntryPoint) // 配置未认证用户访问受保护资源时的EntryPoint
                )

                //配置URL访问权限规则
                .authorizeHttpRequests(authorizeRequests -> {
                    //配置忽略安全控制的URL路径：遍历ignoreUrlsConfig中定义的URL列表
                    for (String url : ignoreUrlsConfig.getUrls()) {
                        //对于列表中的每个URL，允许所有请求（permitAll）访问
                        authorizeRequests.requestMatchers(url).permitAll();
                    }
                    //允许跨域请求的OPTIONS方法通过，因为它们通常不需要认证
                    authorizeRequests.requestMatchers(HttpMethod.OPTIONS).permitAll();
                    //对于任何其他未明确匹配的请求，都需要身份认证（authenticated）
                    authorizeRequests.anyRequest().authenticated();
                });

        //添加JWT认证过滤器到Spring Security过滤器链中
        //它会在UsernamePasswordAuthenticationFilter之前执行，用于从请求头解析JWT并设置认证信息
        httpSecurity.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        //如果配置了动态权限服务和过滤器，则添加动态权限校验过滤器
        //这个过滤器通常用于在基于URL的权限判断之后，进行更细粒度的动态权限检查
        if (dynamicSecurityService != null && dynamicSecurityFilter != null) { // 确保两个都存在
            httpSecurity.addFilterBefore(dynamicSecurityFilter, FilterSecurityInterceptor.class);
        }

        //构建并返回配置好的SecurityFilterChain实例
        //Spring Security会使用这个链来处理进入的HTTP请求
        return httpSecurity.build();
    }

}