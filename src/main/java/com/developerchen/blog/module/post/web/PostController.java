package com.developerchen.blog.module.post.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.developerchen.blog.module.category.domain.entity.Category;
import com.developerchen.blog.module.category.service.ICategoryService;
import com.developerchen.blog.module.post.domain.dto.Archive;
import com.developerchen.blog.module.post.domain.entity.Post;
import com.developerchen.blog.module.post.service.IPostService;
import com.developerchen.core.constant.Const;
import com.developerchen.core.web.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 文章与页面 前端控制器
 *
 * @author syc
 */
@Controller
public class PostController extends BaseController {
    private final IPostService postService;
    private final ICategoryService categoryService;

    public PostController(IPostService postService,
                          ICategoryService categoryService) {
        this.postService = postService;
        this.categoryService = categoryService;
    }

    /**
     * 通过id或者slug获取文章
     *
     * @param idOrSlug 文章主键或者名称
     * @param cp       当前文章评论的页码
     */
    @GetMapping("/post/{idOrSlug}")
    public String post(@PathVariable String idOrSlug,
                       @RequestParam(name = "cp", defaultValue = "1") long cp,
                       Model model) {
        Post post = postService.getPostByIdOrSlug(idOrSlug);
        if (post == null) {
            return page404();
        }
        model.addAttribute("post", post);
        model.addAttribute("cp", cp);
        model.addAttribute("is", "post");
        return "themes/{theme}/post";
    }

    /**
     * 获取指定页面
     *
     * @param id post主键
     * @param cp 当前post评论的页码
     */
    @GetMapping("/page/{id}")
    public String page(@PathVariable long id,
                       @RequestParam(name = "cp", defaultValue = "1") long cp,
                       Model model) {
        Post post = postService.getPostById(id);
        if (post == null) {
            return page404();
        }
        model.addAttribute("post", post);
        model.addAttribute("cp", cp);
        model.addAttribute("is", "page");
        return "themes/{theme}/page";
    }

    /**
     * 归档页, 根据时间归档文章
     */
    @GetMapping("/archives")
    public String archives(Model model) {
        List<Archive> archiveList = postService.getArchiveList();
        model.addAttribute("archives", archiveList);
        model.addAttribute("is", "archives");
        return "themes/{theme}/archives";
    }

    /**
     * 分页形式指定分类下的所有文章
     */
    @GetMapping("/post/category/{categoryId}")
    public String categories(@PathVariable Long categoryId,
                             @RequestParam(defaultValue = "1") long page,
                             @RequestParam(required = false) Long size,
                             Model model) {
        size = size == null ? Const.PAGE_DEFAULT_SIZE : size;
        Category category = categoryService.getCategoryById(categoryId);
        if (category == null) {
            return page404();
        }
        IPage<Post> postPage = postService.getPostPageByCategoryId(categoryId, page, size);
        model.addAttribute("categoryName", category.getName());
        model.addAttribute("postPage", postPage);
        return "themes/{theme}/page-category";
    }

    /**
     * 获取自定义页面
     */
    @GetMapping("/{name:[^.]+}")
    public String page(@PathVariable String name,
                       @RequestParam(name = "cp", defaultValue = "1") long cp,
                       Model model) {
        Post post = postService.getPageBySlug(name);
        if (post == null) {
            return page404();
        }
        model.addAttribute("post", post);
        model.addAttribute("cp", cp);
        model.addAttribute("is", "page");
        return "themes/{theme}/page";
    }
}
