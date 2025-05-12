package com.lzy.mall.security.component;

import cn.hutool.core.util.URLUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import jakarta.annotation.PostConstruct;
import java.util.*;


/**
 * @description: 动态安全元数据源，用于加载和获取URL所需的权限（ConfigAttribute）。
 * 它实现了Spring Security的FilterInvocationSecurityMetadataSource接口，
 * 负责根据当前请求（FilterInvocation）查找与之匹配的安全元数据，即访问该URL资源所需的权限或角色列表。
 * 数据通常从数据库或其他外部源动态加载。
 */
public class DynamicSecurityMetadataSource implements FilterInvocationSecurityMetadataSource {

    // 静态缓存，存储URL模式与所需权限（ConfigAttribute）的映射关系
    // 使用静态是为了在类级别共享，避免重复加载
    private static Map<String, ConfigAttribute> configAttributeMap = null;

    // 用于从外部源（如数据库）加载URL与权限映射的服务
    @Autowired
    private DynamicSecurityService dynamicSecurityService;

    /**
     * @description: 初始化方法，在Bean创建并完成依赖注入后调用。
     * 用于加载URL与权限的映射关系，并填充到静态缓存configAttributeMap中。
     * 使用@PostConstruct确保在服务启动时加载一次。
     */
    @PostConstruct
    public void loadDataSource() {
        // 调用DynamicSecurityService加载数据源
        configAttributeMap = dynamicSecurityService.loadDataSource();
    }

    /**
     * @description: 清除数据源缓存。
     * 当权限配置发生变化时，可以调用此方法清除缓存，以便下次请求时重新加载最新数据。
     */
    public void clearDataSource() {
        // 清空Map
        configAttributeMap.clear();
        // 将Map设为null，标记需要重新加载
        configAttributeMap = null;
    }

    /**
     * @description: 根据给定的安全对象（通常是FilterInvocation，代表HTTP请求）获取所需的安全元数据（ConfigAttribute集合）。
     * Spring Security在进行权限判断时会调用此方法。它会匹配请求的URL与缓存中的URL模式，
     * 返回所有匹配模式对应的权限列表。
     * @param o 安全对象，预期为FilterInvocation
     * @return 访问该对象（URL）所需的权限ConfigAttribute集合
     * @throws IllegalArgumentException 如果传入的安全对象类型不正确
     */
    @Override
    public Collection<ConfigAttribute> getAttributes(Object o) throws IllegalArgumentException {
        // 如果缓存未加载（例如第一次访问或缓存已被清除），则重新加载数据源
        if (configAttributeMap == null)
            this.loadDataSource();
        // 创建一个列表用于存放匹配到的ConfigAttribute（权限）
        List<ConfigAttribute>  configAttributes = new ArrayList<>();
        // 获取当前访问的请求URL
        String url = ((FilterInvocation) o).getRequestUrl();
        // 提取URL中的路径部分（去除查询参数等）
        String path = URLUtil.getPath(url);
        // 创建一个Ant风格路径匹配器，用于匹配URL模式
        PathMatcher pathMatcher = new AntPathMatcher();
        // 获取缓存中所有URL模式（Map的key）的迭代器
        Iterator<String> iterator = configAttributeMap.keySet().iterator();
        // 遍历缓存中的所有URL模式
        while (iterator.hasNext()) {
            // 获取当前的URL模式
            String pattern = iterator.next();
            // 使用路径匹配器检查当前请求的路径是否与该模式匹配
            if (pathMatcher.match(pattern, path)) {
                // 如果匹配成功，则获取该模式对应的ConfigAttribute（权限）并添加到列表中
                configAttributes.add(configAttributeMap.get(pattern));
            }
        }
        // 返回匹配到的权限集合。如果列表为空，表示当前URL在配置中没有找到匹配的权限要求。
        return configAttributes;
    }

    /**
     * @description: 返回此SecurityMetadataSource管理的所有ConfigAttribute。
     * 此方法通常用于某些AccessDecisionManager实现，以便预加载或检查所有可能的权限。
     * 在此实现中，由于权限是动态加载的，且可能数量庞大，故返回null，表示不提供所有权限列表。
     * @return null 表示不提供所有ConfigAttribute列表
     */
    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        // 不提供所有权限的列表
        return null;
    }

    /**
     * @description: 判断此SecurityMetadataSource是否支持给定的安全对象类型。
     * 返回true表示支持所有类型的安全对象，但实际上此实现是为FilterInvocation（Web请求）设计的。
     * @param aClass 需要判断的安全对象类型
     * @return 总是返回true，表示支持处理
     */
    @Override
    public boolean supports(Class<?> aClass) {
        // 支持任何类型的安全对象（尽管通常只处理FilterInvocation）
        return true;
    }

}
