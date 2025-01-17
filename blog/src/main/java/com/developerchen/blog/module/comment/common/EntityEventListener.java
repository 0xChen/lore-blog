package com.developerchen.blog.module.comment.common;

import com.developerchen.blog.module.comment.domain.entity.Comment;
import com.developerchen.blog.module.comment.service.ICommentService;
import com.developerchen.blog.module.post.domain.entity.Post;
import com.developerchen.blog.module.post.service.IPostService;
import com.developerchen.core.event.EntityCreateEvent;
import com.developerchen.core.event.EntityDeleteEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 监听各类事件, 执行相应功能
 *
 * @author syc
 */
@Component
public class EntityEventListener {

    private final ICommentService commentService;
    private final IPostService postService;

    public EntityEventListener(ICommentService commentService,
                               IPostService postService) {
        this.commentService = commentService;
        this.postService = postService;
    }

    /**
     * 监听Post删除事件, 删除该Post对应的Comment
     *
     * @param event 删除post的事件
     */
    @EventListener
    public void deleteComment(EntityDeleteEvent<Post> event) {
        this.commentService.deleteCommentByOwnerId(event.getSource().getId());
    }

    /**
     * 监听Comment新增事件, 增加该Comment对应的Post的评论数量
     *
     * @param event Comment新增事件
     */
    @EventListener
    public void increasePostCommentCount(EntityCreateEvent<Comment> event) {
        Comment comment = event.getSource();
        Long postId = comment.getOwnerId();
        if (postId != null) {
            postService.increasePostCommentCount(postId);
        }
    }

    /**
     * 监听Comment删除事件, 减少该Comment对应的Post的评论数量
     *
     * @param event Comment删除事件
     */
    @EventListener
    public void updatePostCommentCount(EntityDeleteEvent<Comment> event) {
        Comment comment = event.getSource();
        Long postId = comment.getOwnerId();
        if (postId != null) {
            postService.reducePostCommentCount(postId);
        }
    }
}
