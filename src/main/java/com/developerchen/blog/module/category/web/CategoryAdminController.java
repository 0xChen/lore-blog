package com.developerchen.blog.module.category.web;

import com.developerchen.blog.module.category.domain.dto.CategoryDTO;
import com.developerchen.blog.module.category.domain.entity.Category;
import com.developerchen.blog.module.category.service.ICategoryService;
import com.developerchen.core.domain.RestResponse;
import com.developerchen.core.web.BaseController;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分类后台管理控制器
 *
 * @author syc
 */
@RestController
@RequestMapping("/admin")
public class CategoryAdminController extends BaseController {

    private final ICategoryService categoryService;

    public CategoryAdminController(ICategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/categories")
    public RestResponse<?> create(@Validated @RequestBody CategoryDTO categoryDTO) {
        String name = categoryDTO.getName();
        if (StringUtils.isEmpty(name)) {
            return RestResponse.fail("分类名称不能为空");
        }
        Long parentId = categoryDTO.getParentId();
        Category category = categoryService.saveCategory(name, parentId);
        return RestResponse.ok(category);
    }

    @DeleteMapping("/categories/{categoryId}")
    public RestResponse<?> delete(@PathVariable Long categoryId) {
        categoryService.deleteCategoryById(categoryId);
        return RestResponse.ok();
    }

    @PutMapping("/categories/{categoryId}")
    public RestResponse<?> update(@PathVariable Long categoryId,
                                  @Validated @RequestBody Category category,
                                  BindingResult result) {
        if (result.hasErrors()) {
            return RestResponse.fail("数据错误, 更新失败！");
        }
        category.setId(categoryId);
        categoryService.updateCategoryById(category);
        return RestResponse.ok();
    }

    /**
     * 根据条件获取所有符合的分类
     *
     * @param name 分类名称查询条件
     */
    @GetMapping("/categories")
    public RestResponse<List<Category>> getCategory(@RequestParam(required = false) String name) {
        List<Category> categoryList = categoryService.getCategory(name);
        return RestResponse.ok(categoryList);
    }

    /**
     * 获取整个分类树
     */
    @GetMapping("/category/tree")
    public RestResponse<List<CategoryDTO>> getCategoryTree() {
        List<CategoryDTO> categoryDTOList = categoryService.getCategoryTree();
        return RestResponse.ok(categoryDTOList);
    }
}
