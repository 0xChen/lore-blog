package com.developerchen.blog.module.category.repository;

import com.developerchen.blog.module.category.domain.entity.Category;
import com.developerchen.core.repository.CoreMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 分类表 Mapper 接口
 * </p>
 *
 * @author syc
 */
public interface CategoryMapper extends CoreMapper<Category> {

    /**
     * 插入节点前更新相关左值
     *
     * @param rightValue
     */
    @Update("update blog_category set left_value = left_value + 2 where left_value > #{rightValue}")
    void insertUpdateLeftValue(int rightValue);

    /**
     * 插入节点前更新相关右值
     *
     * @param rightValue
     */
    @Update("update blog_category set right_value = right_value + 2 where right_value >= #{rightValue}")
    void insertUpdateRightValue(int rightValue);

    /**
     * 删除节点及其子节点
     *
     * @param rightValue
     */
    @Delete("delete from blog_category where right_value between #{leftValue} and #{rightValue}")
    void deleteByLeftAndRightValue(@Param("leftValue") int leftValue, @Param("rightValue") int rightValue);

    /**
     * 删除节点前更新相关节点左值
     *
     * @param leftValue
     * @param length
     */
    @Update("update blog_category set left_value = left_value - #{length} where left_value > #{leftValue}")
    void deleteUpdateLeftValue(@Param("leftValue") int leftValue, @Param("length") int length);

    /**
     * 删除节点前更新相关节点右值
     *
     * @param rightValue
     * @param length
     */
    @Update("update blog_category set right_value = right_value - #{length} where right_value > #{rightValue}")
    void deleteUpdateRightValue(@Param("rightValue") int rightValue, @Param("length") int length);
}
