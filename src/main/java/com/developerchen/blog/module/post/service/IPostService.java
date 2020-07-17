package com.developerchen.blog.module.post.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.developerchen.blog.module.post.domain.dto.Archive;
import com.developerchen.blog.module.post.domain.dto.PostDTO;
import com.developerchen.blog.module.post.domain.entity.Post;
import com.developerchen.core.service.IBaseService;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * 文章表 服务类
 * </p>
 *
 * @author syc
 */
public interface IPostService extends IBaseService<Post> {
    /**
     * 根据类型获取文章列表
     *
     * @param mode 最新, 随机
     * @param size 获取条数
     * @return 文章集合
     */
    List<Post> getPostList(String mode, long size);

    /**
     * 查询文章归档
     *
     * @return 归档集合
     */
    List<Archive> getArchiveList();

    /**
     * 获取指定状态和类型的文章数量
     *
     * @param status post状态
     * @param type   post类型
     * @return post数量
     */
    int countPost(String status, String type);

    /**
     * 获取标签总数量, 不统计重复标签(标签名称相同视为同一个标签)
     *
     * @return tag 数量
     */
    int countTag();

    /**
     * 获取所有不重复标签(标签名称相同视为同一个标签)
     *
     * @return 标签集合
     */
    Set<String> getTags();

    /**
     * 获取相邻的已发布状态的文章
     *
     * @param type    上一篇:prev | 下一篇:next
     * @param pubdate 当前文章发布时间
     * @return post
     */
    Post getPrevOrNextPost(String type, Date pubdate);

    /**
     * 分页形式获取文章
     *
     * @param title      文章标题查询条件
     * @param status     文章状态查询条件
     * @param type       内容类型查询条件
     * @param tags       标签查询条件
     * @param categoryId 当文章分类查询条件
     * @param page       当前页码
     * @param size       每页数量
     * @return 分页形式的文章集合
     */
    IPage<PostDTO> getPostPage(String title, String status, String type,
                               String tags, Long categoryId, long page, long size);


    /**
     * 分页形式获取指定分类下post
     *
     * @param categoryId 分类
     * @param page       当前页码
     * @param size       每页数量
     * @return IPage<Post>
     */
    IPage<Post> getPostPageByCategoryId(Long categoryId, long page, long size);

    /**
     * 分页形式获取已发布状态文章
     *
     * @param page 当前页码
     * @param size 每页数量
     * @return 分页形式的文章集合
     */
    IPage<Post> getPublishedPostPage(long page, long size);

    /**
     * 根据主键ID获取post
     *
     * @param postId post主键
     * @return PostDTO
     */
    PostDTO getPostDtoById(long postId);

    /**
     * 通过post的唯一name标识获取post, 并发布post被阅读的事件
     *
     * @param postIdOrSlug 主键ID或者name标识
     * @return Post
     */
    Post getPostByIdOrSlug(String postIdOrSlug);

    /**
     * 通过post的slug标识获取post
     *
     * @param slug slug标识
     * @return Post
     */
    Post getPageBySlug(String slug);

    /**
     * 获取指定post
     *
     * @param postId post主键
     * @return Post
     */
    Post getPostById(long postId);

    /**
     * 更新post
     * 如果post状态由[草稿] or [自动草稿] -> [发布]则同时更新post的发布日期
     *
     * @param post Post
     */
    void updatePost(Post post);

    /**
     * 更新 post 状态
     *
     * @param id     post 主键
     * @param status 新状态
     */
    void updateStatusByPostId(long id, String status);

    /**
     * 更新post的阅读次数
     *
     * @param postId post主键
     * @param count  新增加的阅读次数
     */
    void increasePostReadCount(long postId, int count);

    /**
     * 新增post, 并发布Post新增事件
     */
    void savePost(Post post);

    /**
     * 删除指定post, 并发布Post删除事件
     *
     * @param postId post主键
     */
    void deletePostById(long postId);

    /**
     * 批量删除
     *
     * @param postIds 需要删除的记录的主键集合
     */
    void deletePostByIds(Set<Long> postIds);

    /**
     * 增加Post的评论数量
     *
     * @param postId 增加评论数量的post主键
     */
    void increasePostCommentCount(long postId);

    /**
     * 减少Post的评论数量
     *
     * @param postId 减少评论数量的post主键
     */
    void reducePostCommentCount(long postId);

    /**
     * 获取用于构建站点地图的Post
     *
     * @return Post集合
     */
    List<Post> getPostForSitemap();
}
