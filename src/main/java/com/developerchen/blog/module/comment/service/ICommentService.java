package com.developerchen.blog.module.comment.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.developerchen.blog.module.comment.domain.dto.CommentDTO;
import com.developerchen.blog.module.comment.domain.entity.Comment;
import com.developerchen.core.service.IBaseService;

import java.util.List;
import java.util.Set;

/**
 * <p>
 * 评论表 服务类
 * </p>
 *
 * @author syc
 */
public interface ICommentService extends IBaseService<Comment> {
    /**
     * 保存或者更新评论
     *
     * @param comment Comment对象
     * @return boolean
     */
    boolean saveOrUpdateComment(Comment comment);

    /**
     * 回复评论
     *
     * @param comment 回复内容
     */
    void replyComment(Comment comment);

    /**
     * 根据主键删除评论
     *
     * @param commentId 评论表主键
     */
    void deleteCommentById(long commentId);

    /**
     * 根据主键批量删除评论
     *
     * @param commentIds 评论表主键集合
     */
    void deleteCommentByIds(Set<Long> commentIds);

    /**
     * 根据文章主键，删除文章的所有评论
     *
     * @param ownerId 文章表主键
     */
    void deleteCommentByOwnerId(long ownerId);

    /**
     * 更新评论为指定状态
     *
     * @param commentId 评论ID
     * @param status    评论状态
     */
    void updateStatusByCommentId(long commentId, String status);

    /**
     * 更新评论内容
     *
     * @param commentId 评论ID
     * @param content   评论内容
     */
    void updateCommentContent(long commentId, String content);

    /**
     * 最新收到的评论
     *
     * @param size 评论数
     * @return 最新评论的集合
     */
    List<Comment> recentComments(long size);

    /**
     * 获取指定状态的评论数量, 如果没有指定状态则获取所有评论
     *
     * @param status 评论状态
     * @return int 评论数量
     */
    Long countComment(String status);

    /**
     * 查询一条评论
     *
     * @param commentId 评论主键
     * @return Comment
     */
    Comment getCommentById(long commentId);

    /**
     * 获取指定内容下[已批准]状态的评论
     *
     * @param ownerId id
     * @param page    页码
     * @param size    每页条数
     * @return 评论分页数据集合
     */
    IPage<CommentDTO> getCommentsByOwnerId(long ownerId, long page, long size);

    /**
     * 获取指定内容下[已批准]状态的评论
     *
     * @param ownerId id
     * @param page    页码
     * @param size    每页条数
     * @return 评论分页数据集合, 每个评论会记录自己的父级及子级评论, 但是不会嵌套
     */
    IPage<CommentDTO> getFlatCommentsByOwnerId(long ownerId, long page, long size);

    /**
     * 分页形式获取评论
     *
     * @param authorName 评论者名字
     * @param email      电子邮件
     * @param url        网址
     * @param content    评论内容
     * @param status     评论状态
     * @param page       当前页码
     * @param size       每页数量
     * @return 评论分页数据集合
     */
    IPage<Comment> getCommentPage(String authorName,
                                  String email,
                                  String url,
                                  String content,
                                  String status,
                                  long page,
                                  long size);
}
