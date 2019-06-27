package com.developerchen.blog.module.category.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.developerchen.blog.constant.BlogConst;
import com.developerchen.blog.module.category.domain.dto.CategoryDTO;
import com.developerchen.blog.module.category.domain.entity.Category;
import com.developerchen.blog.module.category.repository.CategoryMapper;
import com.developerchen.blog.module.category.service.ICategoryService;
import com.developerchen.core.constant.Const;
import com.developerchen.core.service.impl.BaseServiceImpl;
import org.apache.commons.lang3.Validate;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 分类表 服务实现类
 * </p>
 *
 * @author syc
 */
@Service
public class CategoryServiceImpl extends BaseServiceImpl<CategoryMapper, Category> implements ICategoryService {

    public CategoryServiceImpl() {
    }

    /**
     * 初始化构建根节点
     */
    @Override
    public void initRootCategory() {
        Category root = new Category();
        root.setId(BlogConst.CATEGORY_ROOT_ID);
        root.setName(BlogConst.CATEGORY_ROOT_NAME);
        root.setVisible(Const.YES);
        root.setLeftValue(1);
        root.setRightValue(2);
        baseMapper.insert(root);
    }

    /**
     * 获取分类数量
     *
     * @return int 分类数量
     */
    @Override
    public int categoryCount() {
        return baseMapper.selectCount(null);
    }

    /**
     * 获取所有分类
     *
     * @return 分类集合
     */
    @Override
    public List<Category> getAllCategory() {
        return baseMapper.selectList(new QueryWrapper<Category>().orderByAsc("left_value"));
    }

    /**
     * 将新分类插入到指定父节点下
     *
     * @param name     分类名称
     * @param parentId 新增分类的父分类ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void insertCategory(String name, Long parentId) {
        parentId = parentId == null ? BlogConst.CATEGORY_ROOT_ID : parentId;
        Category parentCategory = baseMapper.selectById(parentId);

        Category category = new Category();
        category.setName(name);
        category.setVisible(Const.YES);
        category.setLeftValue(parentCategory.getRightValue());
        category.setRightValue(parentCategory.getRightValue() + 1);

        baseMapper.insertUpdateLeftValue(parentCategory.getRightValue());
        baseMapper.insertUpdateRightValue(parentCategory.getRightValue());
        baseMapper.insert(category);
    }

    /**
     * 获取指定分类的子分类
     *
     * @param category 父分类
     * @return 子分类集合
     */
    @Override
    public List<Category> getChildren(Category category) {
        QueryWrapper<Category> qw = new QueryWrapper<>();
        qw.gt("left_value", category.getLeftValue());
        qw.lt("right_value", category.getRightValue());
        qw.eq("visible", Const.YES);
        return baseMapper.selectList(qw);
    }

    /**
     * 删除指定分类
     *
     * @param category 要删除的分类
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteCategory(Category category) {
        Long categoryId = category.getId();
        int length = category.getRightValue() - category.getLeftValue() + 1;
        // 必须先执行删除, 在更新相关节点的左右值
        if (categoryId != null) {
            baseMapper.deleteById(category);
        } else {
            baseMapper.deleteByLeftAndRightValue(category.getLeftValue(), category.getRightValue());
        }
        baseMapper.deleteUpdateLeftValue(category.getLeftValue(), length);
        baseMapper.deleteUpdateRightValue(category.getRightValue(), length);
    }

    /**
     * 删除指定分类
     *
     * @param categoryId 要删除的分类ID
     */
    @Override
    public void deleteCategoryById(Long categoryId) {
        Category category = baseMapper.selectById(categoryId);
        deleteCategory(category);
    }

    /**
     * 通过分类名称获取分类树
     *
     * @param name 分类名称
     * @return CategoryDTO
     */
    @Override
    public CategoryDTO getCategoryDTOWithChildren(String name) {
        Category category = getCategoryByName(name);
        // 获取以category为顶点的树并带有深度信息的层序遍历结果集
        List<CategoryDTO> categoryDTOList = getCategoryDTOList(category);
        // 将层序遍历结果集还原成树
        int cursor = 1;
        for (int i = 0; i < categoryDTOList.size() - 1; i++) {
            if (cursor == categoryDTOList.size()) {
                break;
            }
            CategoryDTO parent = categoryDTOList.get(i);
            for (int j = cursor; j < categoryDTOList.size(); j++, cursor = j) {
                CategoryDTO child = categoryDTOList.get(j);
                if (parent.getLevel().equals(child.getLevel())) {
                    // 跳过同级节点
                    continue;
                }
                // 判断此child是不是当前parent的子节点
                if (parent.getLeftValue() < child.getLeftValue()
                        && parent.getRightValue() > child.getRightValue()
                        && (parent.getLevel() - child.getLevel() == 1)) {
                    parent.addChild(child);
                } else {
                    /*
                    之后的child也都不是当前parent的子节点了, 此
                    parent当前level层级的子节点已经构造完毕
                     */
                    break;
                }
            }
        }
        return categoryDTOList.get(0);
        /*Map<Integer, List<CategoryDTO>> levelToCategoryDTOList = new LinkedHashMap<>();
        for (int i = 0; i < categoryDTOList.size(); i++) {
            CategoryDTO categoryDTO = categoryDTOList.get(i);
            Integer level = categoryDTO.getLevel();
            if (levelToCategoryDTOList.containsKey(level)) {
                levelToCategoryDTOList.get(level).add(categoryDTO);
            } else {
                List<CategoryDTO> list = new ArrayList<>();
                list.add(categoryDTO);
                levelToCategoryDTOList.put(level, list);
            }
        }

        // 构造父子结构
        CategoryDTO returnCategoryDTO = categoryDTOList.get(0);
        Integer level = returnCategoryDTO.getLevel();
        while (levelToCategoryDTOList.containsKey(level + 1)) {
            List<CategoryDTO> parents = levelToCategoryDTOList.get(level);
            List<CategoryDTO> children = levelToCategoryDTOList.get(level + 1);
            int cursor = 0;
            for (CategoryDTO parent : parents) {
                for (int i = cursor; i < children.size(); i++, cursor = i) {
                    CategoryDTO child = children.get(i);
                    // 判断该child是不是当前parent的子节点
                    if (parent.getLeftValue() < child.getLeftValue()
                            && parent.getRightValue() > child.getRightValue()) {
                        parent.addChild(child);
                    } else {
                        break;
                    }
                }
            }
        }*/
    }

    /**
     * 根据 主键 查询分类
     *
     * @param id 分类主键
     * @return Category
     */
    @Override
    public Category getCategoryById(Long id) {
        Validate.notNull(id, "类别主键不能为空");
        return baseMapper.selectById(id);
    }

    /**
     * 根据 名称 查询分类
     *
     * @param name 分类名称
     * @return Category
     */
    @Override
    public Category getCategoryByName(String name) {
        Validate.notEmpty(name, "类别名称不能为空");
        return baseMapper.selectOne(new QueryWrapper<Category>().eq("name", name));
    }

    /**
     * 获取category及其所有子节点的带有深度信息的CategoryDTO对象集合
     * 按照自上而下，自左至右的层序遍历方式构建集合
     *
     * @param category 父分类
     * @return 以category为顶点的树的层序遍历结果集
     */
    @Override
    public List<CategoryDTO> getCategoryDTOList(Category category) {
        SQL sql = new SQL()
                .SELECT("n.id AS id",
                        "n.name AS `name`",
                        "n.visible AS visible",
                        "n.left_value AS leftValue",
                        "n.right_value AS rightValue",
                        "count(n.id) AS level")
                .FROM("blog_category n")
                .INNER_JOIN("blog_category p ON n.left_value " +
                        "BETWEEN p.left_value AND p.right_value");
        if (category != null) {
            sql.WHERE("n.left_value >= " + category.getLeftValue());
            sql.WHERE("n.right_value <= " + category.getRightValue());
        } else {
            sql.WHERE("n.left_value >= 0");
        }
        sql.GROUP_BY("n.id").ORDER_BY("level", "leftValue");

        return baseMapper.selectListBySql(sql.toString(), CategoryDTO.class);
    }

    /**
     * 更新指定分类
     *
     * @param category 更新内容
     */
    @Override
    public void updateCategoryById(Category category) {
        baseMapper.updateById(category);
    }
}
