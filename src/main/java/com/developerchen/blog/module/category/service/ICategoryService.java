package com.developerchen.blog.module.category.service;

import com.developerchen.blog.module.category.domain.dto.CategoryDTO;
import com.developerchen.blog.module.category.domain.entity.Category;
import com.developerchen.core.service.IBaseService;

import java.util.List;

/**
 * <p>
 * 分类表 服务类
 * </p>
 *
 * @author syc
 */
public interface ICategoryService extends IBaseService<Category> {

    /**
     * 初始化构建根节点
     */
    void initRootCategory();

    /**
     * 获取分类数量
     *
     * @return int 分类数量
     */
    Long countCategory();

    /**
     * 根据条件获取所有符合的分类
     *
     * @param name 分类名称
     * @return 分类集合
     */
    List<Category> getCategory(String name);

    /**
     * 获取所有分类
     *
     * @return 分类集合
     */
    List<Category> getAllCategory();

    /**
     * 获取分类树
     *
     * @return 分类树
     */
    List<CategoryDTO> getCategoryTree();

    /**
     * 新增分类并插入到指定父节点下
     *
     * @param name     分类名称
     * @param parentId 新增分类的父分类ID, 如果没有父ID, 则插入到默认分类下面
     * @return 新增的分类
     */
    Category saveCategory(String name, Long parentId);

    /**
     * 获取指定分类的子分类
     *
     * @param category 父分类
     * @return 子分类集合
     */
    List<Category> getChildren(Category category);

    /**
     * 根据 主键 查询分类
     *
     * @param id 分类主键
     * @return Category
     */
    Category getCategoryById(long id);

    /**
     * 根据 名称 查询分类
     *
     * @param name 分类名称
     * @return Category
     */
    Category getCategoryByName(String name);

    /**
     * 获取category及其所有子节点的带有深度信息的CategoryDTO对象集合
     *
     * @param category 分类
     * @return CategoryDTO对象集合
     */
    List<CategoryDTO> getCategoryDTOList(Category category);

    /**
     * 通过分类主键获取分类树
     *
     * @param id 分类主键
     * @return CategoryDTO
     */
    CategoryDTO getCategoryDtoWithChildren(long id);

    /**
     * 通过分类名称获取分类树
     *
     * @param name 分类名称
     * @return CategoryDTO
     */
    CategoryDTO getCategoryDtoWithChildren(String name);

    /**
     * 更新指定分类
     *
     * @param category 更新内容
     */
    void updateCategoryById(Category category);

    /**
     * 删除分类
     *
     * @param category 待删除的分类
     */
    void deleteCategory(Category category);

    /**
     * 删除分类
     *
     * @param categoryId 待删除分类的主键
     */
    void deleteCategoryById(long categoryId);
}




