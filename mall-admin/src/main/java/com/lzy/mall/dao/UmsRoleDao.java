package com.lzy.mall.dao;

import com.lzy.mall.model.UmsMenu;
import com.lzy.mall.model.UmsResource;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * UmsRoleDao接口
 * 该接口用于定义与后台角色相关的数据库操作方法，
 * 包括根据用户ID或角色ID获取菜单、根据角色ID获取资源等功能。
 */
public interface UmsRoleDao {
    /**
     * 根据后台用户ID获取菜单
     * 用于查询指定管理员拥有的所有菜单权限
     * @param adminId 管理员ID
     * @return 菜单列表
     */
    List<UmsMenu> getMenuList(@Param("adminId") Long adminId);

    /**
     * 根据角色ID获取菜单
     * 用于查询指定角色拥有的所有菜单权限
     * @param roleId 角色ID
     * @return 菜单列表
     */
    List<UmsMenu> getMenuListByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据角色ID获取资源
     * 用于查询指定角色拥有的所有资源权限
     * @param roleId 角色ID
     * @return 资源列表
     */
    List<UmsResource> getResourceListByRoleId(@Param("roleId") Long roleId);
}
