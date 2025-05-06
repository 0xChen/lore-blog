package com.developerchen.blog.module.category.web.admin;

import com.developerchen.blog.module.category.domain.dto.CategoryDTO;
import com.developerchen.blog.module.category.domain.entity.Category;
import com.developerchen.blog.module.category.service.ICategoryService;
import com.developerchen.core.base.BaseController;
import com.developerchen.core.common.domain.R;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类后台管理控制器
 *
 * @author syc
 */
@RestController
@RequestMapping("/admin/categories")
public class CategoryAdminController extends BaseController {

    private final ICategoryService categoryService;

    public CategoryAdminController(ICategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/save")
    public R<?> save(@Validated @RequestBody CategoryDTO categoryDTO) {
        String name = categoryDTO.getName();
        if (StringUtils.isEmpty(name)) {
            return R.fail("分类名称不能为空");
        }
        Long parentId = categoryDTO.getParentId();
        Category category = categoryService.saveCategory(name, parentId);
        return R.ok(category);
    }

    @PostMapping("/{categoryId}/delete")
    public R<?> delete(@PathVariable Long categoryId) {
        categoryService.deleteCategoryById(categoryId);
        return R.ok();
    }

    @PostMapping("/{categoryId}/update")
    public R<?> update(@PathVariable Long categoryId,
                       @Validated @RequestBody Category category,
                       BindingResult result) {
        if (result.hasErrors()) {
            return R.fail("数据错误, 更新失败！");
        }
        category.setId(categoryId);
        categoryService.updateCategoryById(category);
        return R.ok();
    }

    /**
     * 根据条件获取所有符合的分类
     *
     * @param name 分类名称查询条件
     */
    @GetMapping("/list")
    public R<List<Category>> list(@RequestParam(required = false) String name) {
        List<Category> categoryList = categoryService.getCategoryList(name);
        return R.ok(categoryList);
    }

    /**
     * 获取整个分类树
     */
    @GetMapping("/tree")
    public R<List<CategoryDTO>> tree() {
        List<CategoryDTO> categoryDTOList = categoryService.getCategoryTree();
        return R.ok(categoryDTOList);
    }
}
