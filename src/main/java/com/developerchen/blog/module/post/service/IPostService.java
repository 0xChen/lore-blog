package com.developerchen.blog.module.post.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.developerchen.blog.module.post.domain.dto.Archive;
import com.developerchen.blog.module.post.domain.dto.PostDTO;
import com.developerchen.blog.module.post.domain.entity.Post;
import com.developerchen.core.service.IBaseService;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 文章表 服务类
 * </p>
 *
 * @author syc
 */
public interface IPostService extends IBaseService<Post> {
    List<Post> getPostList(String type, long size);

    List<Archive> getArchiveList();

    int postCount(String status, String type);

    Post getPrevOrNextPost(String type, Date pubdate);

    IPage<Post> getPostPage(String title, String status, String type, Long categoryId, long page, long size);

    IPage<Post> getPostPageByCategoryId(Long categoryId, long page, long size);

    IPage<Post> getPublishedPostPage(long page, long size);

    PostDTO getPostDTObyId(long postId);

    Post getPostByIdOrSlug(String postIdOrSlug);

    Post getPageBySlug(String name);

    Post getPostById(long postId);

    @Transactional
    void updatePost(Post post);

    @Transactional
    void increasePostReadCount(long postId, int count);

    @Transactional
    void savePost(Post post);

    @Transactional
    void deletePostById(long postId);

    @Transactional
    void increasePostCommentCount(long postId);

    @Transactional
    void reducePostCommentCount(long postId);

    List<Post> getPostForSitemap();
}
