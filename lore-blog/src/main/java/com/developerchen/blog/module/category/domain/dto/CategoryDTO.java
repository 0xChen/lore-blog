package com.developerchen.blog.module.category.domain.dto;

import com.developerchen.blog.module.category.domain.entity.Category;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 分类 数据传输对象
 * </p>
 *
 * @author syc
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CategoryDTO extends Category {

    @Serial
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


    public void addChild(CategoryDTO commentDTO) {
        if (children == null) {
            setChildren(new ArrayList<>());
        }
        getChildren().add(commentDTO);
    }

}
