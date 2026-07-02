package com.su.mall.common.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.pagehelper.PageInfo;
import lombok.Data;

import java.util.List;

/**
 * 通用分页数据封装类
 * <p>统一不同分页框架（PageHelper / MyBatis-Plus / Spring Data）的返回格式
 * <p>前端通过此结构获取：当前页码、每页数量、总页数、总条数、数据列表
 *
 * @see #restPage(List) PageHelper分页转换
 * @see #restPage(Page) MyBatis-Plus分页转换
 * @see #restPage(org.springframework.data.domain.Page) Spring Data分页转换
 */
@Data
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
     * 将PageHelper分页后的list转为分页信息
     */
    public static <T> CommonPage<T> restPage(List<T> list) {
        CommonPage<T> result = new CommonPage<>();
        PageInfo<T> pageInfo = new PageInfo<>(list);
        result.setTotalPage(pageInfo.getPages());
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setTotal(pageInfo.getTotal());
        result.setList(pageInfo.getList());
        return result;
    }

    /**
     * 将MyBatis-Plus分页后的Page转为分页信息
     */
    public static <T> CommonPage<T> restPage(Page<T> page) {
        CommonPage<T> result = new CommonPage<>();
        result.setTotalPage((int) page.getPages());
        result.setPageNum((int) page.getCurrent());
        result.setPageSize((int) page.getSize());
        result.setTotal(page.getTotal());
        result.setList(page.getRecords());
        return result;
    }

    /**
     * 将Spring Data分页后的Page转为分页信息
     */
    public static <T> CommonPage<T> restPage(org.springframework.data.domain.Page<T> page) {
        CommonPage<T> result = new CommonPage<>();
        result.setTotalPage(page.getTotalPages());
        result.setPageNum(page.getNumber() + 1);
        result.setPageSize(page.getSize());
        result.setTotal(page.getTotalElements());
        result.setList(page.getContent());
        return result;
    }

}
