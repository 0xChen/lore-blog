package com.developerchen.blog.module.category.domain.dto;

import com.developerchen.blog.module.category.domain.entity.Category;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 分类 数据传输对象
 * </p>
 *
 * @author syc
 */
public class CategoryDTO extends Category {

    private static final long serialVersionUID = 6382532698318127145L;

    /**
     * 层级
     */
    private Integer level;

    /**
     * 父类主键
     */
    private Long parentId;

    /**
     * 子分类
     */
    private List<CategoryDTO> children;

    public CategoryDTO() {
    }

    public CategoryDTO(Category category) {
        BeanUtils.copyProperties(category, this);
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public List<CategoryDTO> getChildren() {
        return children;
    }

    public void setChildren(List<CategoryDTO> children) {
        this.children = children;
    }

    public void addChild(CategoryDTO commentDTO) {
        if (children == null) {
            setChildren(new ArrayList<>());
        }
        getChildren().add(commentDTO);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
