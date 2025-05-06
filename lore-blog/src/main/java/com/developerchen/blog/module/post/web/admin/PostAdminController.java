package com.developerchen.blog.module.post.web.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.developerchen.blog.module.post.domain.dto.PostDTO;
import com.developerchen.blog.module.post.domain.entity.Post;
import com.developerchen.blog.module.post.service.IPostService;
import com.developerchen.core.base.BaseController;
import com.developerchen.core.common.constant.Const;
import com.developerchen.core.common.domain.R;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

/**
 * post & page 后台管理控制器
 *
 * @author syc
 */
@RestController
@RequestMapping("/admin")
public class PostAdminController extends BaseController {
    private final IPostService postService;

    public PostAdminController(IPostService postService) {
        this.postService = postService;
    }

    /**
     * 新增文章或页面
     */
    @PostMapping({"/posts/save", "/pages/save"})
    public R<Post> create(@Validated @RequestBody Post post, BindingResult result) {
        if (result.hasErrors()) {
            return R.fail("保存失败！");
        }
        // 作者
        post.setAuthorId(getUserId());
        postService.savePost(post);
        return R.ok(post);
    }

    /**
     * 更新文章或页面
     */
    @PostMapping({"/posts/update", "/pages/update"})
    public R<Post> update(@Validated @RequestBody Post post, BindingResult result) {
        if (result.hasErrors()) {
            return R.fail("更新失败！");
        }
        postService.updatePost(post);
        return R.ok(post);
    }

    /**
     * 更新文章或页面状态
     */
    @PostMapping({"/posts/{id}/status/update", "/pages/{id}/status/update"})
    public R<Post> status(@PathVariable("id") long id,
                          @RequestBody Map<String, String> parameterMap) {
        postService.updateStatusByPostId(id, parameterMap.get("status"));
        return R.ok();
    }

    /**
     * 分页形式获取文章
     *
     * @param title      文章标题查询条件
     * @param status     文章状态查询条件
     * @param type       内容类型查询条件
     * @param tags       标签
     * @param categoryId 当文章分类查询条件
     * @param page       当前页码
     * @param size       每页数量
     */
    @GetMapping({"/posts/page", "/pages/page"})
    public R<IPage<PostDTO>> page(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(required = false) Long size) {
        size = size == null ? Const.PAGE_DEFAULT_SIZE : size;
        IPage<PostDTO> postDtoPage = postService.getPostPage(title, status, type, tags, categoryId, page, size);

        return R.ok(postDtoPage);
    }

    @GetMapping({"/posts/{id}/detail", "/pages/{id}/detail"})
    public R<Post> post(@PathVariable long id) {
        Post post = postService.getPostById(id);
        return R.ok(post);
    }

    @PostMapping({"/posts/{id}/delete", "/pages/{id}/delete"})
    public R<?> delete(@PathVariable long id) {
        postService.deletePostById(id);
        return R.ok();
    }

    /**
     * 批量删除
     *
     * @param ids 链接ID集合
     */
    @PostMapping({"/posts/{ids}/delete-batch", "/pages/{ids}/delete-batch"})
    public R<?> deleteBatch(@PathVariable Set<Long> ids) {
        postService.deletePostByIds(ids);
        return R.ok();
    }
}
