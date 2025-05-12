package com.lzy.mall.security.component;

import cn.hutool.core.collection.CollUtil;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Iterator;

/**
 * 动态访问决策管理器。
 * <p>
 * 实现 Spring Security 的 {@link AccessDecisionManager} 接口，用于在用户尝试访问受保护的资源时做出最终的访问决策。
 * 它根据当前用户的认证信息（Authentication）和资源所需的权限配置（ConfigAttributes）来判断是否允许访问。
 * <p>
 * 该管理器的工作原理是：遍历资源所需的所有权限，只要用户拥有其中任意一个权限，即判定为允许访问；
 * 如果遍历完所有所需权限后，用户仍未拥有其中任何一个，则拒绝访问。
 * 通常与 {@link org.springframework.security.access.SecurityMetadataSource} 配合使用，
 * SecurityMetadataSource 提供资源所需的 ConfigAttributes。
 */
public class DynamicAccessDecisionManager implements AccessDecisionManager {

    /**
     * 做出访问决策的方法。
     * 这是 AccessDecisionManager 的核心方法，Spring Security 会调用此方法来决定是否允许访问。
     *
     * @param authentication   当前用户的认证信息，包含用户详情和已授予的权限。
     * @param object           被保护的安全对象，例如 FilterInvocation（代表 HTTP 请求）或 MethodInvocation（代表方法调用）。
     * @param configAttributes 与被保护对象关联的配置属性，通常是从 SecurityMetadataSource 获取的访问该资源所需的权限列表。
     * @throws AccessDeniedException             如果用户没有足够的权限访问资源。
     * @throws InsufficientAuthenticationException 如果用户认证信息不足以进行决策（例如用户未认证）。
     */
    @Override
    public void decide(Authentication authentication, Object object,
                       Collection<ConfigAttribute> configAttributes) throws AccessDeniedException, InsufficientAuthenticationException {

        // 当接口未配置任何资源/权限时，configAttributes 集合通常为空或只包含一个表示“无需认证”的属性。
        // CollUtil.isEmpty() 检查集合是否为空。如果为空，表示该资源不需要任何特定权限即可访问。
        if (CollUtil.isEmpty(configAttributes)) {
            // 直接返回，表示放行，允许访问。
            return;
        }

        // 获取所需权限集合的迭代器。
        Iterator<ConfigAttribute> iterator = configAttributes.iterator();

        // 遍历资源所需的所有权限配置。
        while (iterator.hasNext()) {
            // 获取当前的所需权限配置项。
            ConfigAttribute configAttribute = iterator.next();

            // 提取所需权限的字符串表示（例如："ROLE_ADMIN", "permission:user:read"）。
            String needAuthority = configAttribute.getAttribute();

            // 遍历当前认证用户所拥有的所有权限。
            for (GrantedAuthority grantedAuthority : authentication.getAuthorities()) {
                // 将资源所需的权限与用户拥有的权限进行比对。
                // .trim() 去除可能的首尾空白字符，确保精确匹配。
                // .equals() 进行字符串内容比较。
                if (needAuthority.trim().equals(grantedAuthority.getAuthority())) {
                    // 如果找到一个匹配的权限，表示用户拥有访问该资源所需的权限之一。
                    // 直接返回，表示放行，允许访问。
                    return;
                }
            }
            // 如果内层循环（遍历用户权限）结束，仍未找到匹配当前 needAuthority 的权限，外层循环将继续检查下一个所需权限。
        }

        // 如果外层循环（遍历所有所需权限）结束，表示用户没有拥有访问该资源所需的任何一个权限。
        // 抛出 AccessDeniedException 异常，拒绝访问，并提供一个拒绝信息。
        throw new AccessDeniedException("抱歉，您没有访问权限");
    }

    /**
     * 判断此 AccessDecisionManager 实现是否支持给定的 ConfigAttribute。
     * Spring Security 在初始化时会调用此方法，以确定哪个 AccessDecisionManager 应该处理特定的 ConfigAttribute。
     *
     * @param configAttribute 要检查的配置属性。
     * @return 始终返回 true，表示此管理器支持所有类型的 ConfigAttribute。
     */
    @Override
    public boolean supports(ConfigAttribute configAttribute) {
        // 返回 true 表示这个管理器可以处理任何类型的 ConfigAttribute。
        return true;
    }

    /**
     * 判断此 AccessDecisionManager 实现是否支持给定的安全对象类型。
     * Spring Security 在初始化时会调用此方法，以确定哪个 AccessDecisionManager 应该处理哪种类型的安全对象（例如 FilterInvocation.class）。
     *
     * @param aClass 要检查的安全对象类型。
     * @return 始终返回 true，表示此管理器支持所有类型的安全对象。
     */
    @Override
    public boolean supports(Class<?> aClass) {
        // 返回 true 表示这个管理器可以处理任何类型的安全对象。
        return true;
    }
}