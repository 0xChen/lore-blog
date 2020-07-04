package com.developerchen.blog.module.comment.web;

import com.developerchen.blog.module.comment.domain.entity.Comment;
import com.developerchen.blog.module.comment.service.ICommentService;
import com.developerchen.core.domain.RestResponse;
import com.developerchen.core.util.UserUtils;
import com.developerchen.core.web.BaseController;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 评论 前端控制器
 * </p>
 *
 * @author syc
 */
@RestController
public class CommentController extends BaseController {
    private final ICommentService commentService;

    public CommentController(ICommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * 回复评论
     *
     * @param comment 评论内容
     */
    @PostMapping("/comments")
    public RestResponse<?> reply(@Validated Comment comment,
                                 BindingResult result) {
        if (result.hasErrors()) {
            return RestResponse.fail("回复失败！");
        }
        Long userId = getUserId();
        Long authorId = comment.getAuthorId();
        if (authorId == null && userId != null) {
            comment.setAuthorId(userId);
        }
        comment.setIp(UserUtils.getRemoteIp(request));
        comment.setAgent(UserUtils.getUserAgent(request));
        commentService.replyComment(comment);
        return RestResponse.ok();
    }
}

