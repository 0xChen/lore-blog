package com.developerchen.blog.module.comment.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.developerchen.blog.constant.BlogConst;
import com.developerchen.blog.module.comment.domain.entity.Comment;
import com.developerchen.blog.module.comment.service.ICommentService;
import com.developerchen.core.config.AppConfig;
import com.developerchen.core.constant.Const;
import com.developerchen.core.domain.RestResponse;
import com.developerchen.core.domain.entity.User;
import com.developerchen.core.util.UserUtils;
import com.developerchen.core.web.BaseController;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

/**
 * <p>
 * 评论 后台管理控制器
 * </p>
 *
 * @author syc
 */
@RestController
@RequestMapping("/admin")
public class CommentAdminController extends BaseController {
    private final ICommentService commentService;

    public CommentAdminController(ICommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * 获取评论
     */
    @ResponseBody
    @GetMapping("/comments/{commentId}")
    public RestResponse<Comment> link(@PathVariable long commentId) {
        Comment comment = commentService.getCommentById(commentId);
        return RestResponse.ok(comment);
    }

    /**
     * 分页方式获取所有评论
     */
    @GetMapping("/comments")
    public RestResponse<IPage<Comment>> page(@RequestParam(required = false) String authorName,
                                             @RequestParam(required = false) String email,
                                             @RequestParam(required = false) String url,
                                             @RequestParam(required = false) String content,
                                             @RequestParam(required = false) String status,
                                             @RequestParam(defaultValue = "1") Long page,
                                             @RequestParam(required = false) Long size) {
        size = size == null ? Const.PAGE_DEFAULT_SIZE : size;
        IPage<Comment> commentPage = commentService.getCommentPage(authorName,
                email, url, content, status, page, size);
        return RestResponse.ok(commentPage);
    }

    /**
     * 删除指定评论
     *
     * @param commentId 评论ID
     */
    @DeleteMapping("/comments/{commentId}")
    public RestResponse<?> delete(@PathVariable("commentId") long commentId) {
        commentService.deleteCommentById(commentId);
        return RestResponse.ok();
    }

    /**
     * 批量删除评论
     *
     * @param commentIds 评论ID集合
     */
    @ResponseBody
    @DeleteMapping("/comments/{commentIds}/batch")
    public RestResponse<?> deleteBatch(@PathVariable Set<Long> commentIds) {
        commentService.deleteCommentByIds(commentIds);
        return RestResponse.ok();
    }

    /**
     * 更新评论状态
     */
    @PutMapping("/comments/{commentId}/status")
    public RestResponse<?> status(@PathVariable("commentId") long commentId,
                                  @RequestBody Map<String, String> parameterMap) {
        commentService.updateStatusByCommentId(commentId, parameterMap.get("status"));
        return RestResponse.ok();
    }

    /**
     * 回复评论
     */
    @PostMapping("/comments/{commentId}/reply")
    public RestResponse<?> replyComment(@PathVariable("commentId") long commentId,
                                        @RequestBody Comment comment,
                                        BindingResult result) {
        if (result.hasErrors()) {
            return RestResponse.fail("回复失败！");
        }
        User user = UserUtils.getUser();
        if (user != null) {
            comment.setAuthorId(user.getId());
            comment.setAuthorName(user.getNickname());
            comment.setEmail(user.getEmail());
        }
        comment.setParentId(commentId);
        comment.setStatus(BlogConst.COMMENT_STATUS_APPROVED);
        comment.setIp(UserUtils.getRemoteIp(request));
        comment.setAgent(UserUtils.getUserAgent(request));
        comment.setUrl(AppConfig.scheme + "://" + AppConfig.hostname);

        commentService.replyComment(comment);
        return RestResponse.ok();
    }

    /**
     * 更新评论内容
     */
    @PutMapping("/comments/{commentId}/content")
    public RestResponse<?> updateCommentContent(@PathVariable("commentId") long commentId,
                                                @RequestBody Comment comment) {
        commentService.updateCommentContent(commentId, comment.getContent());
        return RestResponse.ok();
    }
}
