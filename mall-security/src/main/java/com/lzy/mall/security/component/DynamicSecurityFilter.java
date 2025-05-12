package com.lzy.mall.security.component;

import com.lzy.mall.security.config.IgnoreUrlsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.SecurityMetadataSource;
import org.springframework.security.access.intercept.AbstractSecurityInterceptor;
import org.springframework.security.access.intercept.InterceptorStatusToken;
import org.springframework.security.web.FilterInvocation;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @description: 动态安全过滤器，用于实现基于URL的动态权限认证。
 * 继承自AbstractSecurityInterceptor，是Spring Security拦截器链的一部分。
 * 它会拦截HTTP请求，并根据配置的忽略URL列表、OPTIONS请求以及动态加载的安全元数据（如资源所需的权限）
 * 来决定是否允许当前用户访问请求的资源。它将权限判断委托给DynamicAccessDecisionManager。
 */
public class DynamicSecurityFilter extends AbstractSecurityInterceptor implements Filter {

    // 动态安全元数据源，用于加载URL与所需权限的映射关系
    @Autowired
    private DynamicSecurityMetadataSource dynamicSecurityMetadataSource;
    // 忽略认证的URL配置，这些URL将直接放行
    @Autowired
    private IgnoreUrlsConfig ignoreUrlsConfig;

    /**
     * @description: 设置访问决策管理器（AccessDecisionManager）。
     * Spring会自动注入DynamicAccessDecisionManager并调用此方法，
     * 将其设置到父类AbstractSecurityInterceptor中，用于后续的权限决策。
     * @param dynamicAccessDecisionManager 自定义的动态访问决策管理器
     */
    @Autowired
    public void setMyAccessDecisionManager(DynamicAccessDecisionManager dynamicAccessDecisionManager) {
        // 调用父类方法设置AccessDecisionManager
        super.setAccessDecisionManager(dynamicAccessDecisionManager);
    }

    /**
     * @description: 过滤器的初始化方法，在过滤器实例创建后调用。
     * 在此实现中，该方法为空，未进行特定的初始化操作。
     * @param filterConfig 过滤器配置对象
     * @throws ServletException 初始化异常
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 无需特定初始化
    }

    /**
     * @description: 执行过滤逻辑。
     * 这是过滤器的核心方法，它会拦截每一个进入的HTTP请求。
     * 方法首先处理OPTIONS请求和白名单（忽略URL）请求，直接放行。
     * 对于其他需要进行安全检查的请求，它委托给父类AbstractSecurityInterceptor的beforeInvocation方法进行处理，
     * beforeInvocation会触发安全元数据查找和访问决策。
     * @param servletRequest HTTP请求对象
     * @param servletResponse HTTP响应对象
     * @param filterChain 过滤器链，用于将请求传递到下一个过滤器或目标资源
     * @throws IOException IO异常
     * @throws ServletException Servlet异常
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        // 将Servlet请求转换为HttpServletRequest
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        // 创建一个FilterInvocation对象，它封装了当前请求、响应和过滤器链，供Spring Security使用
        FilterInvocation fi = new FilterInvocation(servletRequest, servletResponse, filterChain);
        // OPTIONS请求直接放行，因为OPTIONS请求常用于CORS预检，不进行安全认证
        if(request.getMethod().equals(HttpMethod.OPTIONS.toString())){
            // 直接将请求传递到下一个过滤器或目标资源
            fi.getChain().doFilter(fi.getRequest(), fi.getResponse());
            return; // 结束当前过滤器的处理
        }
        // 白名单请求直接放行
        // 创建一个用于匹配URL路径的Ant风格路径匹配器
        PathMatcher pathMatcher = new AntPathMatcher();
        // 遍历配置的忽略URL列表
        for (String path : ignoreUrlsConfig.getUrls()) {
            // 检查当前请求的URI是否与忽略列表中的任一模式匹配
            if(pathMatcher.match(path,request.getRequestURI())){
                // 如果URI匹配忽略模式，则直接将请求传递到下一个过滤器或目标资源
                fi.getChain().doFilter(fi.getRequest(), fi.getResponse());
                return; // 结束当前过滤器的处理
            }
        }
        // 对于非OPTIONS且不在白名单中的请求，此处会调用父类AbstractSecurityInterceptor的beforeInvocation方法。
        // beforeInvocation方法会执行以下步骤：
        // 1. 调用obtainSecurityMetadataSource()获取安全元数据源。
        // 2. 使用安全元数据源查找当前请求URL所需的安全元数据（如权限列表）。
        // 3. 调用AccessDecisionManager（即这里设置的DynamicAccessDecisionManager）的decide方法，
        //    传入Authentication（当前用户信息）、FilterInvocation（请求信息）和安全元数据进行权限判断。
        // 4. 如果decide方法抛出AccessDeniedException，则拦截请求，不再继续后续链。
        // 如果不抛出异常，则表示权限检查通过。
        InterceptorStatusToken token = super.beforeInvocation(fi);
        try {
            // 在安全检查通过后，将请求传递到过滤器链中的下一个环节（可能是其他过滤器或最终的目标Controller）
            fi.getChain().doFilter(fi.getRequest(), fi.getResponse());
        } finally {
            // 调用父类方法进行安全拦截后的处理，用于清理资源或处理异常。
            // 使用finally块确保无论后续过滤器链是否抛出异常，此方法都会被调用。
            super.afterInvocation(token, null);
        }
    }

    /**
     * @description: 过滤器的销毁方法，在过滤器实例被销毁前调用。
     * 在此实现中，该方法为空，未进行特定的清理操作。
     */
    @Override
    public void destroy() {
        // 无需特定清理
    }

    /**
     * @description: 指定当前过滤器处理的安全对象类型。
     * 返回FilterInvocation.class表示此过滤器用于保护基于URL的web资源。
     * @return FilterInvocation.class 表示处理Web请求
     */
    @Override
    public Class<?> getSecureObjectClass() {
        return FilterInvocation.class;
    }

    /**
     * @description: 获取用于查找请求所需权限（安全元数据）的源。
     * 父类AbstractSecurityInterceptor在进行权限判断前会调用此方法获取SecurityMetadataSource。
     * @return DynamicSecurityMetadataSource 自定义的动态安全元数据源
     */
    @Override
    public SecurityMetadataSource obtainSecurityMetadataSource() {
        return dynamicSecurityMetadataSource;
    }

}