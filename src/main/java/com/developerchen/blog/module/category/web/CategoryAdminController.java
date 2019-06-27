package com.developerchen.blog.module.category.web;

import com.developerchen.blog.module.category.domain.dto.CategoryDTO;
import com.developerchen.blog.module.category.domain.entity.Category;
import com.developerchen.blog.module.category.service.ICategoryService;
import com.developerchen.core.domain.RestResponse;
import com.developerchen.core.web.BaseController;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类后台管理控制器
 *
 * @author syc
 */
@RestController
@RequestMapping("/admin/api")
public class CategoryAdminController extends BaseController {

    private final ICategoryService categoryService;

    public CategoryAdminController(ICategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * 获取所有类别
     */
    @GetMapping("/categories")
    public RestResponse allCategory() {
        List<Category> categoryList = categoryService.getAllCategory();
        return RestResponse.ok(categoryList);
    }

    @PutMapping("/category")
    public RestResponse update(@RequestBody Category category) {
        categoryService.updateCategoryById(category);
        return RestResponse.ok();
    }

    @PostMapping("/category")
    public RestResponse create(@RequestBody CategoryDTO categoryDTO) {
        String name = categoryDTO.getName();
        Long parentId = categoryDTO.getParentId();
        categoryService.insertCategory(name, parentId);
        return RestResponse.ok();
    }

    @DeleteMapping("/category/{categoryId}")
    public RestResponse delete(@PathVariable Long categoryId) {
        categoryService.deleteCategoryById(categoryId);
        return RestResponse.ok();
    }

}
