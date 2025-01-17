package com.developerchen.core.domain;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.developerchen.core.constant.Const;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * <p>
 * 分页模型
 * </p>
 * 在 {@link com.baomidou.mybatisplus.extension.plugins.pagination.Page;}的基础上
 * 添加一些扩展属性, 方面页面显示分页导航
 *
 * @author syc
 */
public class RestPage<T> implements IPage<T> {
    @Serial
    private static final long serialVersionUID = -7711850453638062509L;

    /**
     * 查询数据列表
     */
    private List<T> records = Collections.emptyList();

    /**
     * 总数
     */
    private long total = 0;

    /**
     * 每页显示条数，默认 10
     */
    private long size = Const.PAGE_DEFAULT_SIZE;

    /**
     * 当前页
     */
    private long current = 1;
    /**
     * 排序字段信息
     */
    private List<OrderItem> orders = new ArrayList<>();
    /**
     * <p>
     * 自动优化 COUNT SQL
     * </p>
     */
    private boolean optimizeCountSql = true;

    /**
     * <p>
     * 是否进行 count 查询
     * </p>
     */
    private boolean isSearchCount = true;

    /**
     * 当前分页总页数
     */
    private long totalPage;

    /**
     * 每次显示导航页码数量
     */
    private int navPages = 8;

    /**
     * 所有导航页码
     */
    private long[] navPageNums;

    /**
     * 是否存在上一页
     */
    private boolean hasPrev = false;

    /**
     * 是否存在下一页
     */
    private boolean hasNext = false;

    /**
     * 上一页页码
     */
    private long prevPage = 1;

    /**
     * 下一页页码
     */
    private long nextPage = 1;

    /**
     * 是否第一页
     */
    private boolean isFirstPage = false;

    /**
     * 是否最后一页
     */
    private boolean isLastPage = false;

    public RestPage() {
        // to do nothing
    }

    /**
     * <p>
     * 分页构造函数
     * </p>
     *
     * @param current 当前页
     * @param size    每页显示条数
     */

    public RestPage(long current, long size) {
        this(current, size, 0);
    }

    public RestPage(long current, long size, long total) {
        this(current, size, total, true);
    }

    public RestPage(long current, long size, boolean isSearchCount) {
        this(current, size, 0, isSearchCount);
    }

    public RestPage(long current, long size, long total, boolean isSearchCount) {
        if (current > 1) {
            this.current = current;
        }
        this.size = size;
        this.total = total;
        this.isSearchCount = isSearchCount;
    }

    private void init() {
        this.totalPage = this.getPages();

        if (this.current > this.totalPage) {
            this.current = this.totalPage;
        }
        this.calcNavigatePageNumbers();
        this.judgePageBoundary();
    }

    private void calcNavigatePageNumbers() {
        if (this.totalPage <= this.navPages) {
            this.navPageNums = new long[(int) this.totalPage];
            for (int i = 0; i < totalPage; i++) {
                this.navPageNums[i] = i + 1;
            }
        } else {
            this.navPageNums = new long[this.navPages];
            long startNum = this.current - this.navPages / 2;
            long endNum = this.current + this.navPages / 2;
            if (startNum < 1) {
                startNum = 1;
                for (int i = 0; i < this.navPages; i++) {
                    this.navPageNums[i] = startNum++;
                }
            } else if (endNum > this.totalPage) {
                endNum = this.totalPage;
                for (int i = this.navPages - 1; i >= 0; i--) {
                    this.navPageNums[i] = endNum--;
                }
            } else {
                for (int i = 0; i < this.navPages; i++) {
                    this.navPageNums[i] = startNum++;
                }
            }
        }
    }

    private void judgePageBoundary() {
        this.isFirstPage = this.current == 1;
        this.isLastPage = this.current == this.totalPage && this.current != 1;
        this.hasPrev = this.current > 1;
        this.hasNext = this.current < this.totalPage;
        if (this.hasNext) {
            this.nextPage = this.current + 1;
        }
        if (this.hasPrev) {
            this.prevPage = this.current - 1;
        }
    }

    /**
     * <p>
     * 是否存在上一页
     * </p>
     *
     * @return true / false
     */
    public boolean hasPrevious() {
        return this.hasPrev;
    }

    /**
     * <p>
     * 是否存在下一页
     * </p>
     *
     * @return true / false
     */
    public boolean hasNext() {
        return this.hasNext;
    }

    public long getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(long totalPage) {
        this.totalPage = totalPage;
    }

    public int getNavPages() {
        return navPages;
    }

    public void setNavPages(int navPages) {
        this.navPages = navPages;
        this.calcNavigatePageNumbers();
    }

    public long[] getNavPageNums() {
        return navPageNums;
    }

    public void setNavPageNums(long[] navPageNums) {
        this.navPageNums = navPageNums;
    }

    public boolean isHasPrev() {
        return hasPrev;
    }

    public void setHasPrev(boolean hasPrev) {
        this.hasPrev = hasPrev;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public long getPrevPage() {
        return prevPage;
    }

    public void setPrevPage(long prevPage) {
        this.prevPage = prevPage;
    }

    public long getNextPage() {
        return nextPage;
    }

    public void setNextPage(long nextPage) {
        this.nextPage = nextPage;
    }

    public boolean isFirstPage() {
        return isFirstPage;
    }

    public void setFirstPage(boolean firstPage) {
        isFirstPage = firstPage;
    }

    public boolean isLastPage() {
        return isLastPage;
    }

    public void setLastPage(boolean lastPage) {
        isLastPage = lastPage;
    }

    @Override
    public List<T> getRecords() {
        return this.records;
    }

    @Override
    public RestPage<T> setRecords(List<T> records) {
        this.records = records;
        return this;
    }

    @Override
    public long getTotal() {
        return this.total;
    }

    @Override
    public RestPage<T> setTotal(long total) {
        this.total = total;
        init();
        return this;
    }

    @Override
    public long getSize() {
        return this.size;
    }

    @Override
    public RestPage<T> setSize(long size) {
        this.size = size;
        return this;
    }

    @Override
    public long getCurrent() {
        return this.current;
    }

    @Override
    public RestPage<T> setCurrent(long current) {
        this.current = current;
        return this;
    }

    /**
     * 查找 order 中正序排序的字段数组
     *
     * @param filter 过滤器
     * @return 返回正序排列的字段数组
     */
    private String[] mapOrderToArray(Predicate<OrderItem> filter) {
        List<String> columns = new ArrayList<>(orders.size());
        orders.forEach(i -> {
            if (filter.test(i)) {
                columns.add(i.getColumn());
            }
        });
        return columns.toArray(new String[0]);
    }

    /**
     * 移除符合条件的条件
     *
     * @param filter 条件判断
     */
    private void removeOrder(Predicate<OrderItem> filter) {
        for (int i = orders.size() - 1; i >= 0; i--) {
            if (filter.test(orders.get(i))) {
                orders.remove(i);
            }
        }
    }

    /**
     * 添加新的排序条件
     *
     * @param items 条件
     * @return 返回分页参数本身
     */
    public RestPage<T> addOrder(OrderItem... items) {
        orders.addAll(Arrays.asList(items));
        return this;
    }

    @Override
    public List<OrderItem> orders() {
        return getOrders();
    }

    public List<OrderItem> getOrders() {
        return orders;
    }

    public void setOrders(List<OrderItem> orders) {
        this.orders = orders;
    }

    @Override
    public boolean optimizeCountSql() {
        return optimizeCountSql;
    }

    public RestPage<T> setOptimizeCountSql(boolean optimizeCountSql) {
        this.optimizeCountSql = optimizeCountSql;
        return this;
    }

    RestPage<T> setSearchCount(boolean isSearchCount) {
        this.isSearchCount = isSearchCount;
        return this;
    }
}
