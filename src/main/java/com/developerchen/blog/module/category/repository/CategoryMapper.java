package com.developerchen.blog.module.category.repository;

import com.developerchen.blog.module.category.domain.dto.CategoryDTO;
import com.developerchen.blog.module.category.domain.entity.Category;
import com.developerchen.core.repository.CoreMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

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
     * @param rightValue 节点右值
     */
    @Update("update blog_category set left_value = left_value + 2 where left_value > #{rightValue}")
    void updateLeftValueBeforeInsert(int rightValue);

    /**
     * 插入节点前更新相关右值
     *
     * @param rightValue 节点右值
     */
    @Update("update blog_category set right_value = right_value + 2 where right_value >= #{rightValue}")
    void updateRightValueBeforeInsert(int rightValue);

    /**
     * 删除节点及其子节点
     *
     * @param leftValue  节点左值
     * @param rightValue 节点右值
     */
    @Delete("delete from blog_category where right_value between #{leftValue} and #{rightValue}")
    void deleteByLeftAndRightValue(@Param("leftValue") int leftValue, @Param("rightValue") int rightValue);

    /**
     * 删除节点前后更新相关节点左值
     *
     * @param leftValue 节点左值
     * @param length    节点右值 - 节点左值 + 1
     */
    @Update("update blog_category set left_value = left_value - #{length} where left_value > #{leftValue}")
    void updateLeftValueAfterDelete(@Param("leftValue") int leftValue, @Param("length") int length);

    /**
     * 删除节点后更新相关节点右值
     *
     * @param rightValue 节点右值
     * @param length     节点右值 - 节点左值 + 1
     */
    @Update("update blog_category set right_value = right_value - #{length} where right_value > #{rightValue}")
    void updateRightValueAfterDelete(@Param("rightValue") int rightValue, @Param("length") int length);

    @Select("""
                SELECT n.id,
                       n.name,
                       n.visible,
                       n.left_value,
                       n.right_value,
                       COUNT(n.id) AS level
                FROM blog_category n
                INNER JOIN blog_category p ON n.left_value BETWEEN p.left_value AND p.right_value
                WHERE (#{category.leftValue} IS NULL OR n.left_value >= #{category.leftValue})
                  AND (#{category.rightValue} IS NULL OR n.right_value <= #{category.rightValue})
                GROUP BY n.id
                ORDER BY level, left_value
            """)
    List<CategoryDTO> selectCategoryList(@Param("category") Category category);
}
