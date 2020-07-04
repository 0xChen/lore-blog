package com.developerchen.blog.module.post.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.developerchen.blog.module.post.domain.entity.Post;
import com.developerchen.blog.module.post.service.IPostService;
import com.developerchen.core.constant.Const;
import com.developerchen.core.domain.RestResponse;
import com.developerchen.core.web.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * post 后台管理控制器
 *
 * @author syc
 */
@Controller
@RequestMapping("/admin")
public class PostAdminController extends BaseController {
    private final IPostService postService;

    public PostAdminController(IPostService postService) {
        this.postService = postService;
    }

    /**
     * 新增文章
     */
    @ResponseBody
    @PostMapping({"/api/post", "/api/page"})
    public RestResponse create(@Validated @RequestBody Post post, BindingResult result) {
        if (result.hasErrors()) {
            return RestResponse.fail("保存失败！");
        }
        // 作者
        post.setAuthorId(getUserId());
        postService.savePost(post);
        return RestResponse.ok(post);
    }

    /**
     * 更新文章
     */
    @ResponseBody
    @PutMapping({"/api/post", "/api/page"})
    public RestResponse update(@Validated @RequestBody Post post, BindingResult result) {
        if (result.hasErrors()) {
            return RestResponse.fail("更新失败！");
        }
        postService.updatePost(post);
        return RestResponse.ok(post);
    }

    /**
     * 分页形式获取文章
     *
     * @param title      文章标题查询条件
     * @param status     文章状态查询条件
     * @param type       内容类型查询条件
     * @param categoryId 当文章分类查询条件
     * @param page       当前页码
     * @param size       每页数量
     */
    @ResponseBody
    @GetMapping({"/api/posts", "/api/pages"})
    public RestResponse page(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(required = false) Long size) {
        size = size == null ? Const.PAGE_DEFAULT_SIZE : size;
        IPage<Post> postPage = postService.getPostPage(title, status, type, categoryId, page, size);
        return RestResponse.ok(postPage);
    }

    @ResponseBody
    @GetMapping({"/api/post/{id}", "/api/page/{id}"})
    public RestResponse post(@PathVariable long id) {
        Post post = postService.getPostById(id);
        return RestResponse.ok(post);
    }

    @ResponseBody
    @DeleteMapping({"/api/post/{id}", "/api/page/{id}"})
    public RestResponse delete(@PathVariable long id) {
        postService.deletePostById(id);
        return RestResponse.ok();
    }
}
