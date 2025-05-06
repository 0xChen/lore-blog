package com.developerchen.blog.module.comment.web.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.developerchen.blog.constant.BlogConst;
import com.developerchen.blog.module.comment.domain.entity.Comment;
import com.developerchen.blog.module.comment.service.ICommentService;
import com.developerchen.core.base.BaseController;
import com.developerchen.core.common.constant.Const;
import com.developerchen.core.common.domain.R;
import com.developerchen.core.common.util.RequestUtils;
import com.developerchen.core.domain.entity.User;
import com.developerchen.core.system.config.SystemConfig;
import com.developerchen.core.system.util.UserUtils;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
@RestController
@RequestMapping("/admin/comments")
public class CommentAdminController extends BaseController {
    private final ICommentService commentService;


    /**
     * 获取评论
     */
    @ResponseBody
    @GetMapping("/{commentId}/detail")
    public R<Comment> detail(@PathVariable long commentId) {
        Comment comment = commentService.getCommentById(commentId);
        return R.ok(comment);
    }

    /**
     * 分页方式获取所有评论
     */
    @GetMapping("/page")
    public R<IPage<Comment>> page(@RequestParam(required = false) String authorName,
                                  @RequestParam(required = false) String email,
                                  @RequestParam(required = false) String url,
                                  @RequestParam(required = false) String content,
                                  @RequestParam(required = false) String status,
                                  @RequestParam(defaultValue = "1") Long page,
                                  @RequestParam(required = false) Long size) {
        size = size == null ? Const.PAGE_DEFAULT_SIZE : size;
        IPage<Comment> commentPage = commentService.getCommentPage(authorName,
                email, url, content, status, page, size);
        return R.ok(commentPage);
    }

    /**
     * 删除指定评论
     *
     * @param commentId 评论ID
     */
    @PostMapping("/{commentId}/delete")
    public R<?> delete(@PathVariable("commentId") long commentId) {
        commentService.deleteCommentById(commentId);
        return R.ok();
    }

    /**
     * 批量删除评论
     *
     * @param commentIds 评论ID集合
     */
    @PostMapping("/{commentIds}/delete-batch")
    public R<?> deleteBatch(@PathVariable Set<Long> commentIds) {
        commentService.deleteCommentByIds(commentIds);
        return R.ok();
    }

    /**
     * 更新评论状态
     */
    @PostMapping("/{commentId}/status/update")
    public R<?> updateStatus(@PathVariable("commentId") long commentId,
                             @RequestBody Map<String, String> parameterMap) {
        commentService.updateStatusByCommentId(commentId, parameterMap.get("status"));
        return R.ok();
    }

    /**
     * 回复评论
     */
    @PostMapping("/{commentId}/reply")
    public R<?> replyComment(@PathVariable("commentId") long commentId,
                             @RequestBody Comment comment,
                             BindingResult result) {
        if (result.hasErrors()) {
            return R.fail("回复失败！");
        }
        User user = UserUtils.getUser();
        if (user != null) {
            comment.setAuthorId(user.getId());
            comment.setAuthorName(user.getNickname());
            comment.setEmail(user.getEmail());
        }
        comment.setParentId(commentId);
        comment.setStatus(BlogConst.COMMENT_STATUS_APPROVED);
        comment.setIp(RequestUtils.getRemoteIp(request));
        comment.setAgent(RequestUtils.getUserAgent(request));
        comment.setUrl(SystemConfig.scheme + "://" + SystemConfig.hostname);

        commentService.replyComment(comment);
        return R.ok();
    }

    /**
     * 更新评论内容
     */
    @PostMapping("/{commentId}/content/update")
    public R<?> updateCommentContent(@PathVariable("commentId") long commentId,
                                     @RequestBody Comment comment) {
        commentService.updateCommentContent(commentId, comment.getContent());
        return R.ok();
    }
}
