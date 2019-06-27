package com.developerchen.blog.module.comment.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.developerchen.blog.module.comment.domain.entity.Comment;
import com.developerchen.blog.module.comment.service.ICommentService;
import com.developerchen.core.constant.Const;
import com.developerchen.core.domain.RestResponse;
import com.developerchen.core.domain.entity.User;
import com.developerchen.core.util.UserUtils;
import com.developerchen.core.web.BaseController;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 评论 后台管理控制器
 * </p>
 *
 * @author syc
 */
@RestController
@RequestMapping("/admin/api")
public class CommentAdminController extends BaseController {
    private final ICommentService commentService;

    public CommentAdminController(ICommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * 分页方式获取所有评论
     */
    @GetMapping("/comments")
    public RestResponse page(@RequestParam(defaultValue = "1") Long page,
                             @RequestParam(required = false) Long size) {
        size = size == null ? Const.PAGE_DEFAULT_SIZE : size;
        IPage<Comment> commentPage = commentService.getCommentPage(page, size);
        return RestResponse.ok(commentPage);
    }

    /**
     * 删除指定评论
     *
     * @param commentId 评论ID
     */
    @DeleteMapping("/comment/{commentId}")
    public RestResponse delete(@PathVariable("commentId") long commentId) {
        commentService.deleteCommentById(commentId);
        return RestResponse.ok();
    }

    /**
     * 更新评论状态
     */
    @PutMapping("/comment/{commentId}/status")
    public RestResponse status(@PathVariable("commentId") long commentId,
                               @RequestBody Comment comment) {
        commentService.updateStatusByCommentId(commentId, comment.getStatus());
        return RestResponse.ok();
    }

    /**
     * 回复评论
     */
    @PutMapping("/comment/{commentId}/reply")
    public RestResponse reply(@PathVariable("commentId") long commentId,
                              @RequestBody Comment comment) {
        User user = UserUtils.getUser();
        if (user != null) {
            comment.setAuthorId(user.getId());
            comment.setAuthorName(user.getNickname());
            comment.setEmail(user.getEmail());
        }
        comment.setParentId(commentId);
        comment.setIp(UserUtils.getRemoteIp(request));
        comment.setAgent(UserUtils.getUserAgent(request));
        comment.setUrl(Const.HOSTNAME);

        commentService.replyComment(comment);
        return RestResponse.ok();
    }
}

