package com.lzy.mall.bo;

import com.lzy.mall.model.UmsAdmin;
import com.lzy.mall.model.UmsResource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AdminUserDetails类
 * 该类实现了Spring Security的UserDetails接口，用于封装后台管理员的详细信息和权限资源。
 * 主要用于Spring Security的认证和授权流程，将UmsAdmin对象和其拥有的资源列表包装为UserDetails对象，
 * 以便于Spring Security进行权限校验和用户信息管理。
 */
public class AdminUserDetails implements UserDetails {
    // 后台用户对象，包含管理员的基本信息
    private final UmsAdmin umsAdmin;
    // 拥有的资源列表，用于权限控制
    private final List<UmsResource> resourceList;

    /**
     * 构造方法
     * 用于初始化AdminUserDetails对象，传入后台用户和资源列表
     * @param umsAdmin 后台用户对象
     * @param resourceList 拥有的资源列表
     */
    public AdminUserDetails(UmsAdmin umsAdmin, List<UmsResource> resourceList) {
        this.umsAdmin = umsAdmin;
        this.resourceList = resourceList;
    }

    /**
     * 获取当前用户所拥有的权限资源
     * 将资源列表转换为Spring Security所需的GrantedAuthority集合
     * @return 权限集合
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 将每个资源对象转换为SimpleGrantedAuthority对象，格式为"资源ID:资源名称"
        return resourceList.stream()
                .map(resource -> new SimpleGrantedAuthority(resource.getId() + ":" + resource.getName()))
                .collect(Collectors.toList());
    }

    /**
     * 获取当前用户的密码
     * @return 密码字符串
     */
    @Override
    public String getPassword() {
        // 从后台用户对象中获取密码
        return umsAdmin.getPassword();
    }

    /**
     * 获取当前用户的用户名
     * @return 用户名字符串
     */
    @Override
    public String getUsername() {
        // 从后台用户对象中获取用户名
        return umsAdmin.getUsername();
    }

    /**
     * 判断账户是否未过期
     * @return true表示账户未过期
     */
    @Override
    public boolean isAccountNonExpired() {
        // 这里默认账户永不过期
        return true;
    }

    /**
     * 判断账户是否未被锁定
     * @return true表示账户未锁定
     */
    @Override
    public boolean isAccountNonLocked() {
        // 这里默认账户永不锁定
        return true;
    }

    /**
     * 判断凭证是否未过期
     * @return true表示凭证未过期
     */
    @Override
    public boolean isCredentialsNonExpired() {
        // 这里默认凭证永不过期
        return true;
    }

    /**
     * 判断账户是否可用
     * @return true表示账户可用
     */
    @Override
    public boolean isEnabled() {
        // 根据后台用户的状态判断账户是否可用，1为可用
        return umsAdmin.getStatus().equals(1);
    }
}
