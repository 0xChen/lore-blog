package com.developerchen.blog.module.comment.web.api;

import com.developerchen.blog.module.comment.domain.entity.Comment;
import com.developerchen.blog.module.comment.service.ICommentService;
import com.developerchen.core.base.BaseController;
import com.developerchen.core.common.domain.R;
import com.developerchen.core.common.util.RequestUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 评论 前端控制器
 * </p>
 *
 * @author syc
 */
@RestController
@RequestMapping("/comments")
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
    @PostMapping("/reply")
    public R<?> reply(@Validated Comment comment,
                      BindingResult result) {
        if (result.hasErrors()) {
            return R.fail("回复失败！");
        }
        Long userId = getUserId();
        Long authorId = comment.getAuthorId();
        if (authorId == null && userId != null) {
            comment.setAuthorId(userId);
        }
        comment.setIp(RequestUtils.getRemoteIp(request));
        comment.setAgent(RequestUtils.getUserAgent(request));
        commentService.replyComment(comment);
        return R.ok();
    }
}

