package com.lzy.mall.dto; // 定义该类所属的包，通常用于存放数据传输对象

import com.lzy.mall.model.UmsMenu; // 导入父类UmsMenu，表示该类继承自UmsMenu实体
import io.swagger.v3.oas.annotations.media.Schema; // 导入OpenAPI 3.0的Schema注解，用于API文档描述
import lombok.Getter; // 导入Lombok的Getter注解，自动生成所有字段的getter方法
import lombok.Setter; // 导入Lombok的Setter注解，自动生成所有字段的setter方法

import java.util.List; // 导入Java的List集合类，用于存储子菜单列表

/**
 * 后台菜单节点封装类。
 * <p>
 * 该类继承自 `UmsMenu`（后台菜单实体），并在此基础上扩展了一个 `children` 属性。
 * 它的主要作用是将扁平化的菜单列表数据转换为具有层级关系的树形结构，
 * 便于前端页面（如后台管理系统的菜单导航栏）以树状形式展示和操作。
 * 通过Lombok注解自动生成了getter和setter方法，简化了代码。
 */
@Getter // Lombok注解：在编译时自动为该类的所有非静态字段生成公共的getter方法
@Setter // Lombok注解：在编译时自动为该类的所有非静态字段生成公共的setter方法
public class UmsMenuNode extends UmsMenu { // 定义UmsMenuNode类，它继承自UmsMenu类，表示一个菜单节点

    /**
     * 子级菜单列表。
     * <p>
     * 这个属性是该类能够表示树形结构的关键。它存储了当前菜单节点下的所有子菜单节点。
     * 每个子菜单节点也是一个 `UmsMenuNode` 类型的对象，从而形成了递归的树状结构。
     */
    @Schema(description = "子级菜单") // OpenAPI 3.0注解：用于在生成的API文档中描述这个字段的含义
    private List<UmsMenuNode> children; // 定义一个List类型的字段，用于存放当前菜单节点的所有子菜单节点

    /**
     * 在代码中，这会这样体现：
     * UmsMenuNode A (系统管理)：
     * 它的 children 字段会是一个 List，里面包含 UmsMenuNode B、UmsMenuNode E、UmsMenuNode F。
     * UmsMenuNode B (用户管理)：
     * 它的 children 字段会是一个 List，里面包含 UmsMenuNode C、UmsMenuNode D。
     * UmsMenuNode C (用户列表)：
     * 它的 children 字段会是一个 空的 List (因为它没有子菜单了)。
     * UmsMenuNode G (商品管理)：
     * 它的 children 字段会是一个 List，里面包含 UmsMenuNode H、UmsMenuNode I。
     * 这就是“形成无限层级的菜单结构”的含义。 只要父节点有子节点，子节点又可以有自己的子节点，就能一层层地往下扩展，没有固定的深度限制。
     * 为什么需要这种设计？
     * 符合真实世界模型： 后台菜单通常是多级的，这种结构能很好地映射这种层级关系。
     * 方便前端展示： 前端页面拿到这种树形结构的JSON数据后，可以直接通过递归渲染的方式，轻松地展示出多级嵌套的菜单导航栏。
     * 业务逻辑清晰： 在代码层面处理菜单的层级关系（如查找某个菜单的所有子孙菜单、添加删除子菜单等）时，基于这种树形结构会更直观和高效。
     */
}