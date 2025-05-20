package com.lzy.mall.service;

import com.lzy.mall.model.UmsMenu;
import com.lzy.mall.model.UmsResource;
import com.lzy.mall.model.UmsRole;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * UmsRoleService接口
 * 该接口定义了后台角色管理相关的业务操作方法，
 * 包括角色的增删改查、菜单和资源的分配与查询等功能。
 */
public interface UmsRoleService {
    /**
     * 添加角色
     * @param role 角色对象
     * @return 添加结果，成功返回1
     */
    int create(UmsRole role);

    /**
     * 修改角色信息
     * @param id 角色ID
     * @param role 角色对象
     * @return 修改结果，成功返回1
     */
    int update(Long id, UmsRole role);

    /**
     * 批量删除角色
     * @param ids 角色ID列表
     * @return 删除的数量
     */
    int delete(List<Long> ids);

    /**
     * 获取所有角色列表
     * @return 角色列表
     */
    List<UmsRole> list();

    /**
     * 分页获取角色列表
     * @param keyword 查询关键字
     * @param pageSize 每页数量
     * @param pageNum 页码
     * @return 角色列表
     */
    List<UmsRole> list(String keyword, Integer pageSize, Integer pageNum);

    /**
     * 根据管理员ID获取对应菜单
     * @param adminId 管理员ID
     * @return 菜单列表
     */
    List<UmsMenu> getMenuList(Long adminId);

    /**
     * 获取角色相关菜单
     * @param roleId 角色ID
     * @return 菜单列表
     */
    List<UmsMenu> listMenu(Long roleId);

    /**
     * 获取角色相关资源
     * @param roleId 角色ID
     * @return 资源列表
     */
    List<UmsResource> listResource(Long roleId);

    /**
     * 给角色分配菜单
     * @param roleId 角色ID
     * @param menuIds 菜单ID列表
     * @return 分配结果
     */
    @Transactional
    int allocMenu(Long roleId, List<Long> menuIds);

    /**
     * 给角色分配资源
     * @param roleId 角色ID
     * @param resourceIds 资源ID列表
     * @return 分配结果
     */
    @Transactional
    int allocResource(Long roleId, List<Long> resourceIds);
}
