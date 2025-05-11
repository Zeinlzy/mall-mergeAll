package com.lzy.mall.common.api;

import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 通用分页数据封装类
 * CommonPage 是一个通用的分页数据传输对象 (DTO)，
 * 它在项目中扮演着 将不同分页库的结果标准化为统一 API 响应格式 的重要角色，从而提升了 API 的一致性、稳定性和易用性
 */
public class CommonPage<T> {
    /**
     * 当前页码
     */
    private Integer pageNum;
    /**
     * 每页数量
     */
    private Integer pageSize;
    /**
     * 总页数
     */
    private Integer totalPage;
    /**
     * 总条数
     */
    private Long total;
    /**
     * 分页数据
     */
    private List<T> list;

    /**
     * 将PageHelper(常用于 MyBatis)分页后的list转为分页信息
     */
    //公开的静态方法。这意味着你不需要创建 CommonPage 的实例，可以直接通过类名调用它
    public static <T> CommonPage<T> restPage(List<T> list) {

        //创建一个新的 CommonPage 对象实例。这个对象就是我们最终要返回的，用来封装分页信息的容器。
        CommonPage<T> result = new CommonPage<T>();

        //PageInfo 是 PageHelper 库提供的一个类，专门用于封装分页信息
        //PageInfo 能够从这个列表中（因为它是被 PageHelper 包装过的）提取出所有的分页元数据，比如总记录数、总页数、当前页码、每页大小等
        PageInfo<T> pageInfo = new PageInfo<T>(list);

        //从 PageInfo 对象 (pageInfo) 中获取总页数 (pageInfo.getPages())。
        //将获取到的总页数设置到 result (CommonPage 对象) 的 totalPage 字段中。
        result.setTotalPage(pageInfo.getPages());

        //从 pageInfo 中获取当前页码 (pageInfo.getPageNum())。
        //将获取到的当前页码设置到 result 的 pageNum 字段中。
        result.setPageNum(pageInfo.getPageNum());

        //从 pageInfo 中获取每页显示的数量 (pageInfo.getPageSize())。
        //将获取到的每页数量设置到 result 的 pageSize 字段中。
        result.setPageSize(pageInfo.getPageSize());

        //从 pageInfo 中获取总记录数 (pageInfo.getTotal())。
        //将获取到的总记录数设置到 result 的 total 字段中。注意 PageInfo 的 getTotal() 返回的是 long 类型，这与 CommonPage 的 total 字段类型一致。
        result.setTotal(pageInfo.getTotal());

        //从 PageInfo 中获取当前页的数据列表 (pageInfo.getList())。
        //将获取到的数据列表设置到 result 的 list 字段中。
        result.setList(pageInfo.getList());

        //返回构建并填充好所有分页信息的 CommonPage 对象。
        return result;
    }

    /**
     * 将SpringData(自带的 Page 对象)分页后的list转为分页信息
     */
    public static <T> CommonPage<T> restPage(Page<T> pageInfo) {

        //创建一个新的 CommonPage 对象实例，用来存储转换后的分页信息。
        CommonPage<T> result = new CommonPage<T>();

        result.setTotalPage(pageInfo.getTotalPages());
        result.setPageNum(pageInfo.getNumber());
        result.setPageSize(pageInfo.getSize());
        result.setTotal(pageInfo.getTotalElements());
        result.setList(pageInfo.getContent());
        return result;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(Integer totalPage) {
        this.totalPage = totalPage;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }
}
