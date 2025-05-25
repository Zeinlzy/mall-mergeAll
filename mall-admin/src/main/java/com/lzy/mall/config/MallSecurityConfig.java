package com.lzy.mall.config;

import com.lzy.mall.model.UmsResource;
import com.lzy.mall.security.component.DynamicSecurityService;
import com.lzy.mall.service.UmsAdminService;
import com.lzy.mall.service.UmsResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 商城安全配置类
 * 负责配置Spring Security相关的安全认证和授权机制
 */
@Configuration
public class MallSecurityConfig {

    /**
     * 注入用户管理服务，用于处理用户认证相关操作
     */
    @Autowired
    private UmsAdminService adminService;

    /**
     * 注入资源管理服务，用于获取系统资源权限配置
     */
    @Autowired
    private UmsResourceService resourceService;

    /**
     * 配置用户详情服务Bean
     * Spring Security在进行用户认证时会调用此服务来获取用户信息
     *
     * @return UserDetailsService 用户详情服务实例
     */
    @Bean
    public UserDetailsService userDetailsService() {
        // 获取登录用户信息
        // 使用Lambda表达式实现UserDetailsService接口
        // 当用户登录时，根据用户名从数据库中加载用户详细信息
        return username -> adminService.loadUserByUsername(username);
    }

    /**
     * 配置动态安全服务Bean
     * 用于动态加载系统资源权限配置，实现基于URL的动态权限控制
     *
     * @return DynamicSecurityService 动态安全服务实例
     */
    @Bean
    public DynamicSecurityService dynamicSecurityService() {
        return new DynamicSecurityService() {
            /**
             * 加载资源权限数据源
             * 从数据库中获取所有资源信息，构建URL与权限的映射关系
             *
             * @return Map<String, ConfigAttribute> URL路径与权限配置的映射
             */
            @Override
            public Map<String, ConfigAttribute> loadDataSource() {
                // 使用ConcurrentHashMap确保线程安全
                Map<String, ConfigAttribute> map = new ConcurrentHashMap<>();

                // 从数据库获取所有资源列表
                List<UmsResource> resourceList = resourceService.listAll();

                // 遍历资源列表，构建URL与权限的映射关系
                for (UmsResource resource : resourceList) {
                    // 将资源URL作为key，资源ID和名称组合作为权限标识
                    // 格式：资源ID:资源名称（如：1:用户管理）
                    map.put(resource.getUrl(),
                            new org.springframework.security.access.SecurityConfig(
                                    resource.getId() + ":" + resource.getName()));
                }

                return map;
            }
        };
    }
}