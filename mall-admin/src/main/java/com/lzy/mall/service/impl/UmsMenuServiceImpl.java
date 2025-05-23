package com.lzy.mall.service.impl; // 定义该类所属的包，表示它是服务层实现类

import com.github.pagehelper.PageHelper; // 导入PageHelper分页插件，用于简化分页操作
import com.lzy.mall.dto.UmsMenuNode; // 导入菜单的树形结构数据传输对象
import com.lzy.mall.mapper.UmsMenuMapper; // 导入菜单数据访问接口，用于与数据库交互
import com.lzy.mall.model.UmsMenu; // 导入菜单实体类
import com.lzy.mall.model.UmsMenuExample; // 导入菜单查询条件的Example类
import com.lzy.mall.service.UmsMenuService; // 导入菜单服务接口
import org.springframework.beans.BeanUtils; // 导入Spring工具类，用于对象属性的复制
import org.springframework.beans.factory.annotation.Autowired; // 导入Spring的自动注入注解
import org.springframework.stereotype.Service; // 导入Spring的服务注解

import java.util.Date; // 导入Java日期类
import java.util.List; // 导入Java列表工具类
import java.util.stream.Collectors; // 导入Stream API的收集器，用于集合操作

/**
 * 后台菜单管理Service实现类。
 * <p>
 * 该类实现了 `UmsMenuService` 接口，提供了后台菜单的各项业务逻辑功能。
 * 它通过依赖注入 `UmsMenuMapper` 来与数据库进行交互，执行菜单数据的增、删、改、查操作。
 * 此外，还包含了菜单层级（level）的计算、分页查询以及将扁平化菜单列表转换为树形结构等复杂业务逻辑。
 */
@Service // 标记这个类是一个Spring的服务组件，Spring容器会自动扫描并管理它
public class UmsMenuServiceImpl implements UmsMenuService {

    // 自动注入 `UmsMenuMapper` 接口的实现类。
    // `UmsMenuMapper` 是由MyBatis等持久层框架生成的，用于直接与数据库进行交互，执行SQL操作。
    @Autowired
    private UmsMenuMapper menuMapper;

    /**
     * 创建后台菜单。
     * <p>
     * 在菜单创建时，会自动设置创建时间，并根据父菜单ID计算并设置菜单的层级。
     *
     * @param umsMenu 待创建的菜单实体对象，包含菜单的各种属性。
     * @return 数据库操作影响的行数，通常为1表示成功，0表示失败。
     */
    @Override // 标记该方法是实现父接口 UmsMenuService 中的方法
    public int create(UmsMenu umsMenu) {
        // 设置菜单的创建时间为当前系统时间。
        umsMenu.setCreateTime(new Date());
        // 调用私有辅助方法 `updateLevel`，根据菜单的父ID计算并设置其层级（level）。
        updateLevel(umsMenu);
        // 调用Mapper接口的insert方法，将完整的菜单对象插入到数据库中。
        return menuMapper.insert(umsMenu);
    }

    /**
     * 计算并更新菜单的层级（level）。
     * <p>
     * 菜单层级表示菜单在树形结构中的深度，根菜单的层级为0。
     * 如果菜单没有父菜单（parentId为0），则为一级菜单（level=0）。
     * 如果有父菜单，则其层级为父菜单的层级加1。
     *
     * @param umsMenu 待更新层级的菜单对象。
     */
    private void updateLevel(UmsMenu umsMenu) {
        // 判断当前菜单是否是根菜单（parentId为0表示没有父菜单）。
        if (umsMenu.getParentId() == 0) {
            // 如果是根菜单，则将其层级设置为0。
            umsMenu.setLevel(0);
        } else {
            // 如果有父菜单，则需要根据父菜单的层级来设置当前菜单的层级。
            // 先通过父菜单ID从数据库查询父菜单的完整信息。
            UmsMenu parentMenu = menuMapper.selectByPrimaryKey(umsMenu.getParentId());
            // 检查是否成功查询到父菜单。
            if (parentMenu != null) {
                // 如果父菜单存在，则当前菜单的层级为父菜单层级加1。
                umsMenu.setLevel(parentMenu.getLevel() + 1);
            } else {
                // 如果父菜单不存在（例如，传入的parentId无效），
                // 暂时将其层级设置为0（或根据业务需求抛出异常/进行其他处理）。
                umsMenu.setLevel(0);
            }
        }
    }

    /**
     * 更新后台菜单信息。
     * <p>
     * 根据菜单ID更新其详细信息，并重新计算菜单的层级。
     *
     * @param id      待更新菜单的ID。
     * @param umsMenu 包含更新后菜单信息的实体对象。
     * @return 数据库操作影响的行数，通常为1表示成功，0表示失败。
     */
    @Override // 标记该方法是实现父接口 UmsMenuService 中的方法
    public int update(Long id, UmsMenu umsMenu) {
        // 将URL路径中的ID设置到待更新的菜单对象中，确保更新的是正确的记录。
        umsMenu.setId(id);
        // 调用私有辅助方法 `updateLevel`，重新计算并设置菜单的层级，
        // 以防在更新时修改了父ID导致层级变化。
        updateLevel(umsMenu);
        // 调用Mapper接口的updateByPrimaryKeySelective方法，
        // 根据主键ID有选择地更新菜单对象中非空的字段。
        return menuMapper.updateByPrimaryKeySelective(umsMenu);
    }

    /**
     * 根据ID获取菜单详情。
     * <p>
     * 查询指定ID的后台菜单的详细信息。
     *
     * @param id 菜单的ID。
     * @return 匹配指定ID的菜单实体对象，如果不存在则返回null。
     */
    @Override // 标记该方法是实现父接口 UmsMenuService 中的方法
    public UmsMenu getItem(Long id) {
        // 调用Mapper接口的selectByPrimaryKey方法，根据主键ID查询菜单详情。
        return menuMapper.selectByPrimaryKey(id);
    }

    /**
     * 根据ID删除后台菜单。
     * <p>
     * 删除指定ID的后台菜单。
     *
     * @param id 待删除菜单的ID。
     * @return 数据库操作影响的行数，通常为1表示成功，0表示失败。
     */
    @Override // 标记该方法是实现父接口 UmsMenuService 中的方法
    public int delete(Long id) {
        // 调用Mapper接口的deleteByPrimaryKey方法，根据主键ID删除菜单。
        return menuMapper.deleteByPrimaryKey(id);
    }

    /**
     * 分页查询后台菜单列表。
     * <p>
     * 根据父菜单ID进行过滤，并支持分页显示。
     *
     * @param parentId 父菜单的ID。用于查询某个父级菜单下的所有直接子菜单。
     * @param pageSize 每页显示的记录数。
     * @param pageNum  当前查询的页码。
     * @return 包含当前页菜单列表的List。
     */
    @Override // 标记该方法是实现父接口 UmsMenuService 中的方法
    public List<UmsMenu> list(Long parentId, Integer pageSize, Integer pageNum) {
        // 使用PageHelper插件启动分页功能。
        // 它会自动拦截后续的查询操作，添加分页SQL。
        PageHelper.startPage(pageNum, pageSize);
        // 创建一个`UmsMenuExample`对象，用于构建查询条件。
        UmsMenuExample example = new UmsMenuExample();
        // 设置查询结果的排序规则：按照`sort`字段降序排列。
        example.setOrderByClause("sort desc");
        // 获取Criteria对象，并添加查询条件：`parentId`必须等于传入的`parentId`。
        example.createCriteria().andParentIdEqualTo(parentId);
        // 调用Mapper接口的selectByExample方法执行查询，
        // PageHelper会在此时应用分页逻辑。
        return menuMapper.selectByExample(example);
    }

    /**
     * 获取所有菜单的树形结构列表。
     * <p>
     * 首先查询所有扁平化的菜单数据，然后通过递归的方式构建成树形结构。
     *
     * @return 包含根菜单节点及其所有子孙菜单的树形结构列表。
     */
    @Override // 标记该方法是实现父接口 UmsMenuService 中的方法
    public List<UmsMenuNode> treeList() {
        // 1. 查询所有后台菜单的扁平化列表。
        // 这里不添加任何条件，表示查询所有菜单记录。
        List<UmsMenu> menuList = menuMapper.selectByExample(new UmsMenuExample());

        // 2. 使用Java 8 Stream API将扁平列表转换为树形结构。
        List<UmsMenuNode> result = menuList.stream()
                // 过滤出所有父ID为0的菜单（即顶级菜单或根菜单）。
                .filter(menu -> menu.getParentId().equals(0L))
                // 将每个顶级菜单转换为UmsMenuNode，并递归地设置其所有子菜单。
                .map(menu -> covertMenuNode(menu, menuList))
                // 将转换后的UmsMenuNode对象收集成一个列表。
                .collect(Collectors.toList());
        // 返回包含树形结构菜单的列表。
        return result;
        /**
         * 总结 treeList() 方法的工作流程可以概括为：
         * 1.一次性查询所有菜单数据。 (扁平化列表)
         * 2.筛选出所有顶级菜单。 (作为树的根)
         * 3.对每个顶级菜单，递归地构建它的子树。 (利用 covertMenuNode 方法的递归特性)
         * 4.将所有构建好的顶级菜单树收集起来，作为最终结果返回。
         */
    }

    /**
     * 修改菜单的显示状态（隐藏/显示）。
     * <p>
     * 根据菜单ID更新其`hidden`属性。
     *
     * @param id     待修改状态菜单的ID。
     * @param hidden 菜单的新显示状态：`0`表示显示，`1`表示隐藏。
     * @return 数据库操作影响的行数，通常为1表示成功，0表示失败。
     */
    @Override // 标记该方法是实现父接口 UmsMenuService 中的方法
    public int updateHidden(Long id, Integer hidden) {
        // 创建一个UmsMenu对象，只设置ID和要更新的hidden状态。
        // 这样可以只更新这两个字段，避免影响其他字段。
        UmsMenu umsMenu = new UmsMenu();
        umsMenu.setId(id);
        umsMenu.setHidden(hidden);
        // 调用Mapper接口的updateByPrimaryKeySelective方法，
        // 根据主键ID有选择地更新菜单的hidden字段。
        return menuMapper.updateByPrimaryKeySelective(umsMenu);
    }

    /**
     * 辅助方法：将 `UmsMenu` 实体转换为 `UmsMenuNode` 树形节点，并递归设置其子节点。
     * <p>
     * 这个方法是构建菜单树形结构的核心，它会递归地查找并设置当前菜单的子菜单。
     *
     * @param menu     当前需要转换和处理的 `UmsMenu` 对象。
     * @param menuList 包含所有扁平化菜单的完整列表，用于递归查找子菜单。
     * @return 转换后的 `UmsMenuNode` 对象，其 `children` 属性已填充（如果存在子菜单）。
     */
    private UmsMenuNode covertMenuNode(UmsMenu menu, List<UmsMenu> menuList) {
        // 1. 创建一个新的UmsMenuNode对象。
        UmsMenuNode node = new UmsMenuNode();
        // 2. 将当前UmsMenu对象的所有属性复制到UmsMenuNode对象中。
        // 这样，UmsMenuNode就继承了UmsMenu的所有基本信息（如ID、名称、路径等）。
        BeanUtils.copyProperties(menu, node);

        // 3. 递归查找并设置当前节点的子菜单。
        List<UmsMenuNode> children = menuList.stream()
                // 从完整的菜单列表中筛选出所有父ID等于当前菜单ID的子菜单。
                .filter(subMenu -> subMenu.getParentId().equals(menu.getId()))
                // 对每个找到的子菜单，递归调用covertMenuNode方法，
                // 将其转换为UmsMenuNode并构建其子树。
                .map(subMenu -> covertMenuNode(subMenu, menuList))
                // 将所有转换后的子菜单节点收集成一个列表。
                .collect(Collectors.toList());
        // 4. 将构建好的子菜单列表设置到当前UmsMenuNode的children属性中。
        node.setChildren(children);
        // 5. 返回构建完成的UmsMenuNode（包含其所有子孙节点）。
        return node;
    }
}