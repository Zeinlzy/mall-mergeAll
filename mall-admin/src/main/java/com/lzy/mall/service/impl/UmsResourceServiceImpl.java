package com.lzy.mall.service.impl;

import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageHelper;
import com.lzy.mall.mapper.UmsResourceMapper;
import com.lzy.mall.model.UmsResource;
import com.lzy.mall.model.UmsResourceExample;
import com.lzy.mall.service.UmsAdminCacheService;
import com.lzy.mall.service.UmsResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 后台资源管理Service实现类
 * 负责处理资源相关的业务逻辑，包括资源的增删改查操作
 * 并管理相关的缓存数据，确保数据一致性
 */
@Service
public class UmsResourceServiceImpl implements UmsResourceService {

    /**
     * 资源数据访问对象，负责与数据库进行交互
     * 提供基础的CRUD操作方法
     */
    @Autowired
    private UmsResourceMapper resourceMapper;

    /**
     * 管理员缓存服务，负责维护管理员相关的缓存数据
     * 当资源发生变更时，需要清理相关缓存以保证数据一致性
     */
    @Autowired
    private UmsAdminCacheService adminCacheService;

    /**
     * 创建新的资源记录
     * 自动设置创建时间为当前时间
     *
     * @param umsResource 要创建的资源对象，包含资源的基本信息
     * @return int 返回插入操作影响的记录数
     *         - 1：插入成功
     *         - 0：插入失败
     */
    @Override
    public int create(UmsResource umsResource) {
        // 设置资源创建时间为当前时间
        umsResource.setCreateTime(new Date());

        // 执行插入操作并返回影响的记录数
        return resourceMapper.insert(umsResource);
    }

    /**
     * 更新指定ID的资源信息
     * 只更新非空字段，避免覆盖原有数据
     * 更新后清理相关缓存确保数据一致性
     *
     * @param id 要更新的资源ID
     * @param umsResource 包含更新信息的资源对象
     * @return int 返回更新操作影响的记录数
     *         - 1：更新成功
     *         - 0：更新失败或记录不存在
     */
    @Override
    public int update(Long id, UmsResource umsResource) {
        // 设置资源ID，确保更新指定记录
        umsResource.setId(id);

        // 执行选择性更新操作（只更新非null字段）
        int count = resourceMapper.updateByPrimaryKeySelective(umsResource);

        // 清理该资源相关的管理员缓存数据
        // 因为资源变更可能影响管理员的权限配置
        adminCacheService.delResourceListByResource(id);

        return count;
    }

    /**
     * 根据ID获取资源详细信息
     *
     * @param id 资源ID
     * @return UmsResource 资源对象
     *         - 存在时返回完整的资源信息
     *         - 不存在时返回null
     */
    @Override
    public UmsResource getItem(Long id) {
        // 根据主键ID查询资源记录
        return resourceMapper.selectByPrimaryKey(id);
    }

    /**
     * 根据ID删除资源记录
     * 删除后清理相关缓存确保数据一致性
     *
     * @param id 要删除的资源ID
     * @return int 返回删除操作影响的记录数
     *         - 1：删除成功
     *         - 0：删除失败或记录不存在
     */
    @Override
    public int delete(Long id) {
        // 执行删除操作
        int count = resourceMapper.deleteByPrimaryKey(id);

        // 清理该资源相关的管理员缓存数据
        // 删除资源后，相关权限配置也需要从缓存中移除
        adminCacheService.delResourceListByResource(id);

        return count;
    }

    /**
     * 分页条件查询资源列表
     * 支持多种条件组合查询，包括分类筛选和关键字模糊匹配
     *
     * @param categoryId 资源分类ID，可为null表示不按分类筛选
     * @param nameKeyword 资源名称关键字，用于模糊匹配资源名称
     * @param urlKeyword URL关键字，用于模糊匹配资源URL
     * @param pageSize 每页显示的记录数
     * @param pageNum 页码，从1开始
     * @return List<UmsResource> 符合条件的资源列表
     *         使用PageHelper插件实现分页，返回当前页的数据
     */
    @Override
    public List<UmsResource> list(Long categoryId, String nameKeyword, String urlKeyword, Integer pageSize, Integer pageNum) {
        // 启动分页功能，设置当前页码和每页记录数
        PageHelper.startPage(pageNum, pageSize);

        // 创建查询条件构建器
        UmsResourceExample example = new UmsResourceExample();
        UmsResourceExample.Criteria criteria = example.createCriteria();

        // 根据分类ID进行精确匹配（如果提供了分类ID）
        if (categoryId != null) {
            criteria.andCategoryIdEqualTo(categoryId);
        }

        // 根据资源名称进行模糊匹配（如果提供了名称关键字）
        if (StrUtil.isNotEmpty(nameKeyword)) {
            // 使用LIKE查询，前后加%实现模糊匹配
            criteria.andNameLike('%' + nameKeyword + '%');
        }

        // 根据资源URL进行模糊匹配（如果提供了URL关键字）
        if (StrUtil.isNotEmpty(urlKeyword)) {
            // 使用LIKE查询，前后加%实现模糊匹配
            criteria.andUrlLike('%' + urlKeyword + '%');
        }

        // 执行查询并返回结果
        // PageHelper会自动处理分页逻辑，只返回当前页的数据
        return resourceMapper.selectByExample(example);
    }

    /**
     * 查询所有资源记录
     * 不分页，返回系统中的全部资源数据
     * 通常用于权限配置、下拉选择等需要完整数据的场景
     *
     * @return List<UmsResource> 所有资源的完整列表
     *         按数据库中的默认排序返回
     */
    @Override
    public List<UmsResource> listAll() {
        // 创建空的查询条件，表示查询所有记录
        // 不使用PageHelper，返回全部数据
        return resourceMapper.selectByExample(new UmsResourceExample());
    }
}