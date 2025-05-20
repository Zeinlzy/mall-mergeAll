package com.lzy.mall.dao;

import com.lzy.mall.model.UmsAdminRoleRelation;
import com.lzy.mall.model.UmsResource;
import com.lzy.mall.model.UmsRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * UmsAdminRoleRelationDao接口
 * 该接口用于定义后台管理员与角色、资源关系相关的数据库操作方法。
 * 包含批量插入用户角色关系、获取用户角色列表、获取用户资源列表、获取资源相关用户ID列表等功能。
 */
public interface UmsAdminRoleRelationDao {
    /**
     * 批量插入用户角色关系
     * 用于将多个用户角色关系一次性插入数据库，提高插入效率。
     * @param adminRoleRelationList 用户角色关系对象列表
     * @return 插入的记录数
     */
    int insertList(@Param("list") List<UmsAdminRoleRelation> adminRoleRelationList);

    /**
     * 获取指定管理员拥有的所有角色列表
     * @param adminId 管理员ID
     * @return 该管理员拥有的角色列表
     */
    List<UmsRole> getRoleList(@Param("adminId") Long adminId);

    /**
     * 获取指定管理员拥有的所有可访问资源列表
     * @param adminId 管理员ID
     * @return 该管理员可访问的资源列表
     */
    List<UmsResource> getResourceList(@Param("adminId") Long adminId);

    /**
     * 获取拥有指定资源的所有管理员ID列表
     * @param resourceId 资源ID
     * @return 拥有该资源的管理员ID列表
     */
    List<Long> getAdminIdList(@Param("resourceId") Long resourceId);
}
