package com.developerchen.blog.module.category.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.developerchen.blog.constant.BlogConst;
import com.developerchen.blog.exception.BlogException;
import com.developerchen.blog.module.category.domain.dto.CategoryDTO;
import com.developerchen.blog.module.category.domain.entity.Category;
import com.developerchen.blog.module.category.repository.CategoryMapper;
import com.developerchen.blog.module.post.domain.entity.Post;
import com.developerchen.blog.module.post.service.IPostService;
import com.developerchen.core.constant.Const;
import com.developerchen.core.service.impl.BaseServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 分类表 服务实现类
 * </p>
 *
 * @author syc
 */
@Service
public class CategoryServiceImpl extends BaseServiceImpl<CategoryMapper, Category> implements ICategoryService {

    @Autowired(required = false)
    private IPostService postService;

    public CategoryServiceImpl() {
    }

    /**
     * 初始化构建根节点
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
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
    public Long countCategory() {
        return baseMapper.selectCount(null);
    }

    /**
     * 根据条件获取所有符合的分类
     *
     * @param name 分类名称
     * @return 分类集合
     */
    @Override
    public List<Category> getCategory(String name) {
        QueryWrapper<Category> qw = new QueryWrapper<>();
        qw.like(StringUtils.isNotBlank(name), "name", name);
        qw.orderByAsc("left_value");

        return baseMapper.selectList(qw);
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
     * 获取分类树
     * 采用与 getCategoryDtoWithChildren 方法不同的思路遍历构造树
     *
     * @return 分类树
     */
    @Override
    public List<CategoryDTO> getCategoryTree() {
        List<CategoryDTO> categoryDTOList = getCategoryDTOList(null);

        Map<Integer, List<CategoryDTO>> levelToCategoryDTOList = categoryDTOList.stream()
                .collect(Collectors.groupingBy(
                        CategoryDTO::getLevel,
                        LinkedHashMap::new,
                        Collectors.toList()));

        // 构造父子结构
        List<CategoryDTO> topNodeList = null;
        for (Map.Entry<Integer, List<CategoryDTO>> entry : levelToCategoryDTOList.entrySet()) {
            Integer level = entry.getKey();
            List<CategoryDTO> parents = entry.getValue();
            if (topNodeList == null) {
                topNodeList = parents;
            }

            // 判断是否有子分类
            if (levelToCategoryDTOList.containsKey(level + 1)) {
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

            }

        }

        return topNodeList;
    }

    /**
     * 将新分类插入到指定父节点下
     *
     * @param name     分类名称
     * @param parentId 新增分类的父分类ID
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    @Override
    public Category saveCategory(String name, Long parentId) {
        QueryWrapper<Category> qw = new QueryWrapper<>();
        qw.eq("name", name);
        Long count = baseMapper.selectCount(qw);
        if (count > 0) {
            throw new BlogException("新增失败, 已经存在 \"" + name + "\" 这个分类, 请换个分类名称");
        }

        parentId = parentId == null ? BlogConst.CATEGORY_ROOT_ID : parentId;
        Category parentCategory = baseMapper.selectById(parentId);

        Category category = new Category();
        category.setName(name);
        category.setVisible(Const.YES);
        category.setLeftValue(parentCategory.getRightValue());
        category.setRightValue(parentCategory.getRightValue() + 1);

        baseMapper.updateLeftValueBeforeInsert(parentCategory.getRightValue());
        baseMapper.updateRightValueBeforeInsert(parentCategory.getRightValue());
        baseMapper.insert(category);
        return category;
    }

    /**
     * 获取指定分类的所有子分类
     *
     * @param category 父分类
     * @return 所有子分类的集合
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
    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void deleteCategory(Category category) {
        Long categoryId = category.getId();
        if (BlogConst.CATEGORY_ROOT_ID.equals(categoryId)) {
            throw new BlogException("\"默认分类\"不允许删除. ");
        }

        // 被文章使用的分类不允许删除
        if (postService != null) {
            List<Category> children = getChildren(category);
            List<Long> categoryIdList = children.stream().map(Category::getId).collect(Collectors.toList());
            categoryIdList.add(categoryId);


            QueryWrapper<Post> qw = new QueryWrapper<>();
            qw.select("category_id");
            qw.in("category_id", categoryIdList);
            List<Post> postList = postService.list(qw);

            if (postList.size() > 0) {
                Map<Long, String> categoryIdToName = children.stream()
                        .collect(Collectors.toMap(Category::getId, Category::getName));
                categoryIdToName.put(category.getId(), category.getName());

                String categoryName = postList.stream().map(post -> categoryIdToName.get(post.getCategoryId()))
                        .distinct().collect(Collectors.joining(", "));
                throw new BlogException("删除失败, 因为 \"" + categoryName +
                        "\" 已经被使用, 请先删除相关文章及页面或更改它们的分类后再删除分类");
            }
        }

        int length = category.getRightValue() - category.getLeftValue() + 1;
        // 必须先执行删除, 在更新相关节点的左右值
        baseMapper.deleteByLeftAndRightValue(category.getLeftValue(), category.getRightValue());
        baseMapper.updateLeftValueAfterDelete(category.getLeftValue(), length);
        baseMapper.updateRightValueAfterDelete(category.getRightValue(), length);
    }

    /**
     * 删除指定分类
     *
     * @param categoryId 要删除的分类ID
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    @Override
    public void deleteCategoryById(long categoryId) {
        Category category = baseMapper.selectById(categoryId);
        if (category == null) {
            throw new BlogException("删除失败, 不存在此分类. ");
        }
        deleteCategory(category);
    }

    /**
     * 通过分类主键获取分类树
     *
     * @param id 分类主键
     * @return CategoryDTO
     */
    @Override
    public CategoryDTO getCategoryDtoWithChildren(long id) {
        Category category = getCategoryById(id);
        return getCategoryDtoWithChildren(category);
    }

    /**
     * 通过分类名称获取分类树
     *
     * @param name 分类名称
     * @return CategoryDTO
     */
    @Override
    public CategoryDTO getCategoryDtoWithChildren(String name) {
        Category category = getCategoryByName(name);
        return getCategoryDtoWithChildren(category);
    }

    /**
     * 通过分类获取其分类树
     *
     * @param category 分类
     * @return CategoryDTO
     */
    private CategoryDTO getCategoryDtoWithChildren(Category category) {
        // 获取以category为顶点的树并带有深度信息的层序遍历结果集
        List<CategoryDTO> categoryDtoList = getCategoryDTOList(category);
        // 将层序遍历结果集还原成树
        int cursor = 1;
        for (int i = 0; i < categoryDtoList.size() - 1; i++) {
            if (cursor == categoryDtoList.size()) {
                break;
            }
            CategoryDTO parent = categoryDtoList.get(i);
            for (int j = cursor; j < categoryDtoList.size(); j++, cursor = j) {
                CategoryDTO child = categoryDtoList.get(j);
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
        return categoryDtoList.get(0);
    }

    /**
     * 根据 主键 查询分类
     *
     * @param id 分类主键
     * @return Category
     */
    @Override
    public Category getCategoryById(long id) {
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
        Validate.notEmpty(name, "分类名称不能为空");
        return baseMapper.selectOne(new QueryWrapper<Category>().eq("name", name));
    }

    /**
     * 获取category及其所有子节点的带有深度信息的CategoryDTO对象集合
     * 按照自上而下，自左至右的层序遍历方式构建集合
     *
     * @param category 父分类, 为 null 时查所有
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
    @Transactional(rollbackFor = {Exception.class, Error.class})
    @Override
    public void updateCategoryById(Category category) {
        String name = category.getName();
        if (name != null) {
            QueryWrapper<Category> qw = new QueryWrapper<>();
            qw.ne("id", category.getId());
            qw.eq("name", name);
            Long count = baseMapper.selectCount(qw);
            if (count > 0) {
                throw new BlogException("更新失败, 已经存在 \"" + name + "\" 这个分类, 请换个分类名称");
            }
        }
        baseMapper.updateById(category);
    }
}
