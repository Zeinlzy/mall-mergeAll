package com.lzy.mall.service.impl;

import com.lzy.mall.mapper.UmsMemberLevelMapper;
import com.lzy.mall.model.UmsMemberLevel;
import com.lzy.mall.model.UmsMemberLevelExample;
import com.lzy.mall.service.UmsMemberLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * 会员等级服务接口 `UmsMemberLevelService` 的实现类。
 * <p>
 * 该类是业务逻辑层（Service Layer）的核心组成部分，负责处理会员等级相关的具体业务操作。
 * 它通过依赖注入的方式，使用 `UmsMemberLevelMapper` 与数据库进行数据交互，
 * 实现数据的查询、增删改查等功能。
 */
@Service // 标记这个类为一个Spring的服务组件，Spring容器会自动扫描并管理它
public class UmsMemberLevelServiceImpl implements UmsMemberLevelService {

    // 自动注入 `UmsMemberLevelMapper` 接口的实现类。
    // `UmsMemberLevelMapper` 通常是由MyBatis等持久层框架生成的，
    // 用于直接与数据库进行交互，执行SQL操作。
    @Autowired
    private UmsMemberLevelMapper memberLevelMapper;

    /**
     * 根据会员等级的默认状态（defaultStatus）查询会员等级列表。
     * <p>
     * 此方法实现了会员等级的过滤查询功能。
     * 客户端可以通过传入不同的 `defaultStatus` 值来获取特定类型的会员等级：
     * - 当 `defaultStatus` 为 `1` 时，表示查询系统默认的会员等级（如“普通会员”）。
     * - 当 `defaultStatus` 为 `0` 时，表示查询非默认的会员等级（如“黄金会员”、“白金会员”等，这些通常是需要满足一定条件才能达到的等级）。
     *
     * @param defaultStatus 会员等级的默认状态标识：`1` 表示默认等级，`0` 表示非默认等级。
     * @return 符合指定 `defaultStatus` 条件的会员等级列表。
     */
    @Override // 标记该方法是实现父接口 UmsMemberLevelService 中的方法
    public List<UmsMemberLevel> list(Integer defaultStatus) {
        // 1. 创建一个 `UmsMemberLevelExample` 对象。
        //    这个Example对象是MyBatis（或其他ORM框架）中用于构建动态SQL查询条件的关键。
        //    它允许我们以编程方式定义WHERE子句、ORDER BY子句等。
        UmsMemberLevelExample example = new UmsMemberLevelExample();

        // 2. 构建查询条件。
        //    `createCriteria()` 方法用于获取或创建一个用于添加查询条件的Criteria对象。
        //    `andDefaultStatusEqualTo(defaultStatus)` 则在Criteria对象中添加了一个条件：
        //    要求数据库表中的 `default_status` 字段的值与传入的 `defaultStatus` 参数相等。
        example.createCriteria().andDefaultStatusEqualTo(defaultStatus);

        // 3. 执行数据库查询并返回结果。
        //    调用 `memberLevelMapper` 的 `selectByExample()` 方法，将构建好的 `example` 对象传入。
        //    Mapper会根据这个 `example` 对象生成并执行对应的SQL查询，
        //    然后将查询结果（多条会员等级记录）映射成 `List<UmsMemberLevel>` 对象列表并返回。
        return memberLevelMapper.selectByExample(example);
    }
}
