package com.developerchen.blog.module.comment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.developerchen.blog.constant.BlogConst;
import com.developerchen.blog.module.comment.domain.dto.CommentDTO;
import com.developerchen.blog.module.comment.domain.entity.Comment;
import com.developerchen.blog.module.comment.repository.CommentMapper;
import com.developerchen.blog.module.comment.service.ICommentService;
import com.developerchen.core.event.EntityCreateEvent;
import com.developerchen.core.event.EntityDeleteEvent;
import com.developerchen.core.service.impl.BaseServiceImpl;
import com.developerchen.core.util.RestPage;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 评论表 服务实现类
 * </p>
 *
 * @author syc
 */
@Service
public class CommentServiceImpl extends BaseServiceImpl<CommentMapper, Comment> implements ICommentService {

    public CommentServiceImpl() {
    }

    /**
     * 保存或者更新评论
     *
     * @param comment Comment对象
     */
    @Override
    public boolean saveOrUpdate(Comment comment) {
        return super.saveOrUpdate(comment);
    }

    /**
     * 回复评论
     *
     * @param comment 回复内容
     */
    @Override
    public void replyComment(Comment comment) {
        Long ownerId = comment.getOwnerId();
        if (ownerId == null) {
            Long parentCommentId = comment.getParentId();
            Comment parentComment = baseMapper.selectById(parentCommentId);
            comment.setOwnerId(parentComment.getOwnerId());
        }

        // 审核机制还没有实做, 暂默认[已批准]状态
        comment.setStatus(BlogConst.COMMENT_APPROVED);
        baseMapper.insert(comment);
        eventPublisher.publishEvent(new EntityCreateEvent<>(comment));
    }

    /**
     * 根据主键删除评论
     *
     * @param commentId 评论表主键
     */
    @Override
    public void deleteCommentById(long commentId) {
        Comment comment = baseMapper.selectById(commentId);
        if (comment != null) {
            baseMapper.deleteById(commentId);
            eventPublisher.publishEvent(new EntityDeleteEvent<>(comment));
        }
    }

    /**
     * 根据文章主键，删除文章的所有评论
     *
     * @param ownerId 文章表主键
     */
    @Override
    public void deleteCommentByOwnerId(long ownerId) {
        Map<String, Object> conditionMap = new HashMap<>(16);
        conditionMap.put("owner_id", ownerId);
        baseMapper.deleteByMap(conditionMap);
    }

    /**
     * 更新评论为指定状态
     *
     * @param id     评论ID
     * @param status 评论状态
     */
    @Override
    public void updateStatusByCommentId(long id, String status) {
        Validate.notNull(status, "评论状态不能为空!");

        Comment comment = new Comment();
        comment.setId(id);
        comment.setStatus(status);
        baseMapper.updateById(comment);
    }

    /**
     * 最新收到的评论
     *
     * @param size 评论数
     * @return 最新评论的集合
     */
    @Override
    public List<Comment> recentComments(long size) {
        int maxRecentComments = 10;
        size = (size < 0 || size > maxRecentComments) ? maxRecentComments : size;

        IPage<Comment> commentPage = baseMapper.selectPage(
                new Page<>(0, size),
                new QueryWrapper<Comment>()
                        .eq("status", BlogConst.COMMENT_APPROVED)
                        .orderByDesc("create_time")
        );
        return commentPage.getRecords();
    }

    /**
     * 获取指定状态的评论数量, 如果没有指定状态则获取所有评论
     *
     * @param status 评论状态
     * @return int 评论数量
     */
    @Override
    public int commentCount(String status) {
        QueryWrapper<Comment> qw = new QueryWrapper<>();
        qw.eq(status != null, "status", status);
        return baseMapper.selectCount(qw);
    }

    /**
     * 查询一条评论
     *
     * @param id 评论主键
     * @return Comment
     */
    @Override
    public Comment getCommentById(long id) {
        return baseMapper.selectById(id);
    }

    /**
     * 获取指定内容下[审批通过]状态的评论
     *
     * @param ownerId id
     * @param page    页码
     * @param size    每页条数
     * @return 评论分页数据集合
     */
    @Override
    public IPage<CommentDTO> getCommentsByOwnerId(long ownerId, long page, long size) {
        QueryWrapper<Comment> qw = new QueryWrapper<>();
        qw.eq("owner_id", ownerId);
        qw.eq("status", BlogConst.COMMENT_APPROVED);
        qw.orderByDesc("create_time");

        // 每个Post下评论数量级不会很大, 直接一次性获取Post下所有评论
        List<Comment> commentList = baseMapper.selectList(qw);
        // 1级评论
        IPage<Comment> commentPage = baseMapper.selectPage(
                new Page<>(page, size), qw.isNull("owner_id"));

        Map<Long, List<Comment>> commentIdToChildren = new HashMap<>((int) (commentPage.getSize() / 0.75) + 1);
        for (Comment comment : commentList) {
            Long parentId = comment.getParentId();
            if (parentId == null) {
                continue;
            }
            if (commentIdToChildren.containsKey(parentId)) {
                commentIdToChildren.get(parentId).add(comment);
            } else {
                List<Comment> list = new ArrayList<>();
                list.add(comment);
                commentIdToChildren.put(parentId, list);
            }
        }
        List<CommentDTO> commentDTOList = commentPage.getRecords()
                .stream()
                .map(comment -> {
                    CommentDTO commentDTO = new CommentDTO(comment);
                    setChildrenForCommentDTO(commentIdToChildren, commentDTO, 1);
                    return commentDTO;
                }).collect(Collectors.toList());
        RestPage<CommentDTO> commentDTOPage = new RestPage<>(
                commentPage.getCurrent(),
                commentPage.getSize());
        commentDTOPage.setTotal(commentPage.getTotal());
        commentDTOPage.setRecords(commentDTOList);
        return commentDTOPage;
    }

    /**
     * 递归为CommentDTO添加children子评论
     *
     * @param commentIdToChildren 评论主键 -> 子评论集合的映射
     * @param commentDTO          需要添加子评论的评论
     * @param level               层级
     * @return 评论最大层级
     */
    private int setChildrenForCommentDTO(
            Map<Long, List<Comment>> commentIdToChildren,
            CommentDTO commentDTO, int level) {
        int maxLevel = level;
        Long commentId = commentDTO.getId();
        if (!commentIdToChildren.containsKey(commentId)) {
            commentDTO.setLevel(level);
            return maxLevel;
        }
        List<Comment> children = commentIdToChildren.get(commentId);
        List<CommentDTO> commentDTOList = children.stream()
                .map(CommentDTO::new)
                .collect(Collectors.toList());

        commentDTO.setLevel(level);
        commentDTO.setChildren(commentDTOList);
        for (CommentDTO dto : commentDTOList) {
            int currentLevel = setChildrenForCommentDTO(commentIdToChildren, dto, level + 1);
            maxLevel = currentLevel > maxLevel ? currentLevel : maxLevel;
        }
        return maxLevel;
    }

    /**
     * 获取指定内容下[审批通过]状态的评论
     *
     * @param ownerId id
     * @param page    页码
     * @param size    每页条数
     * @return 评论分页数据集合, 每个评论会记录自己的父级及子级评论, 但是不会嵌套
     */
    @Override
    public IPage<CommentDTO> getFlatCommentsByOwnerId(long ownerId, long page, long size) {

        QueryWrapper<Comment> qw = new QueryWrapper<>();
        qw.eq("owner_id", ownerId);
        qw.eq("status", BlogConst.COMMENT_APPROVED);
        qw.orderByDesc("create_time");

        IPage<Comment> commentPage = baseMapper.selectPage(new Page<>(page, size), qw);

        Set<Long> commentIdSet = new HashSet<>();
        Set<Long> parentCommentIdSet = new HashSet<>();
        commentPage.getRecords().forEach(comment -> {
            Long id = comment.getId();
            Long parentId = comment.getParentId();
            commentIdSet.add(id);
            if (parentId != null) {
                parentCommentIdSet.add(parentId);
            }
        });

        Map<Long, Comment> parentIdToComment = new HashMap<>((int) (parentCommentIdSet.size() / 0.75) + 1);
        if (parentCommentIdSet.size() > 0) {
            // 获取当前分页中评论的父评论
            QueryWrapper<Comment> parentCommentQW = new QueryWrapper<>();
            parentCommentQW.in("id", parentCommentIdSet);
            List<Comment> parentCommentList = baseMapper.selectList(parentCommentQW);
            parentCommentList.forEach(comment -> {
                Long id = comment.getId();
                parentIdToComment.put(id, comment);
            });
        }
        // 获取当前分页中评论的子评论
        List<Comment> childrenCommentList = new ArrayList<>();
        if (commentIdSet.size() > 0) {
            QueryWrapper<Comment> childrenCommentQW = new QueryWrapper<>();
            childrenCommentQW.in("parent_id", commentIdSet);
            childrenCommentList = baseMapper.selectList(childrenCommentQW);
        }

        // 构造评论 -> 子评论的Map映射
        Map<Long, List<CommentDTO>> commentIdToChildren = new HashMap<>((int) (commentPage.getSize() / 0.75) + 1);
        for (Comment child : childrenCommentList) {
            Long parentId = child.getParentId();
            if (commentIdToChildren.containsKey(parentId)) {
                commentIdToChildren.get(parentId).add(new CommentDTO(child));
            } else {
                List<CommentDTO> list = new ArrayList<>();
                list.add(new CommentDTO(child));
                commentIdToChildren.put(parentId, list);
            }
        }

        List<CommentDTO> commentDTOList = commentPage.getRecords()
                .stream()
                .map(comment -> {
                    Long commentId = comment.getId();
                    Long parentId = comment.getParentId();

                    Comment parent = parentIdToComment.get(parentId);
                    List<CommentDTO> children = commentIdToChildren.get(commentId);

                    CommentDTO commentDTO = new CommentDTO(comment);
                    commentDTO.setParent(parent);
                    commentDTO.setChildren(children);

                    return commentDTO;
                }).collect(Collectors.toList());

        RestPage<CommentDTO> commentDTOPage = new RestPage<>(
                commentPage.getCurrent(),
                commentPage.getSize());
        commentDTOPage.setTotal(commentPage.getTotal());
        commentDTOPage.setRecords(commentDTOList);
        return commentDTOPage;
    }

    /**
     * 分页形式获取评论
     *
     * @param page 当前页码
     * @param size 每页数量
     */
    @Override
    public IPage<Comment> getCommentPage(long page, long size) {
        return baseMapper.selectPage(
                new RestPage<>(page, size),
                new QueryWrapper<Comment>().orderByDesc("create_time")
        );
    }

}
