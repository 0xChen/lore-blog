package com.developerchen.blog.module.comment.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.developerchen.blog.config.BlogConfig;
import com.developerchen.blog.constant.BlogConst;
import com.developerchen.blog.exception.BlogException;
import com.developerchen.blog.module.comment.domain.dto.CommentDTO;
import com.developerchen.blog.module.comment.domain.entity.Comment;
import com.developerchen.blog.module.comment.repository.CommentMapper;
import com.developerchen.core.constant.Const;
import com.developerchen.core.domain.RestPage;
import com.developerchen.core.event.EntityCreateEvent;
import com.developerchen.core.event.EntityDeleteEvent;
import com.developerchen.core.service.impl.BaseServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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


    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public boolean saveOrUpdateComment(Comment comment) {
        return super.saveOrUpdate(comment);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void replyComment(Comment comment) {
        Long ownerId = comment.getOwnerId();
        if (ownerId == null) {
            Long parentCommentId = comment.getParentId();
            Comment parentComment = baseMapper.selectById(parentCommentId);
            comment.setOwnerId(parentComment.getOwnerId());
        }

        if (comment.getStatus() == null) {
            if (Const.YES.equals(BlogConfig.ALLOW_COMMENT_APPROVE)) {
                // 如果启用了评论审批机制, 设置默认[待批准]状态
                comment.setStatus(BlogConst.COMMENT_STATUS_PENDING);
            } else {
                comment.setStatus(BlogConst.COMMENT_STATUS_APPROVED);
            }
        }

        baseMapper.insert(comment);
        eventPublisher.publishEvent(new EntityCreateEvent<>(comment));
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void deleteCommentById(long commentId) {
        Comment comment = baseMapper.selectById(commentId);
        if (comment != null) {
            baseMapper.deleteById(commentId);
            eventPublisher.publishEvent(new EntityDeleteEvent<>(comment));
        } else {
            throw new BlogException("删除失败, 此评论已不存在. ");
        }
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void deleteCommentByIds(Set<Long> commentIds) {
        List<Comment> commentList = baseMapper.selectBatchIds(commentIds);
        if (commentList.size() != commentIds.size()) {
            throw new BlogException("删除失败, 因某些评论已不存在, 请刷新页面后重试. ");
        }

        if (commentList.size() > 0) {
            baseMapper.deleteBatchIds(commentIds);
            eventPublisher.publishEvent(new EntityDeleteEvent<>(commentList));
        }
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void deleteCommentByOwnerId(long ownerId) {
        Map<String, Object> conditionMap = new HashMap<>(16);
        conditionMap.put("owner_id", ownerId);
        baseMapper.deleteByMap(conditionMap);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void updateStatusByCommentId(long commentId, String status) {
        Validate.notNull(status, "评论状态不能为空!");

        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setStatus(status);
        baseMapper.updateById(comment);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void updateCommentContent(long commentId, String content) {
        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setContent(content);
        baseMapper.updateById(comment);
    }

    @Override
    public List<Comment> recentComments(long size) {
        int maxRecentComments = 10;
        size = (size < 0 || size > maxRecentComments) ? maxRecentComments : size;

        IPage<Comment> commentPage = baseMapper.selectPage(
                new Page<>(0, size),
                new QueryWrapper<Comment>()
                        .eq("status", BlogConst.COMMENT_STATUS_APPROVED)
                        .orderByDesc("create_time")
        );
        return commentPage.getRecords();
    }

    @Override
    public int countComment(String status) {
        QueryWrapper<Comment> qw = new QueryWrapper<>();
        qw.eq(status != null, "status", status);
        return baseMapper.selectCount(qw);
    }

    @Override
    public Comment getCommentById(long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public IPage<CommentDTO> getCommentsByOwnerId(long ownerId, long page, long size) {
        QueryWrapper<Comment> qw = new QueryWrapper<>();
        qw.eq("owner_id", ownerId);
        qw.eq("status", BlogConst.COMMENT_STATUS_APPROVED);
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
                    setChildrenForCommentDto(commentIdToChildren, commentDTO, 1);
                    return commentDTO;
                }).collect(Collectors.toList());
        RestPage<CommentDTO> commentDtoPage = new RestPage<>(
                commentPage.getCurrent(),
                commentPage.getSize());
        commentDtoPage.setTotal(commentPage.getTotal());
        commentDtoPage.setRecords(commentDTOList);
        return commentDtoPage;
    }

    /**
     * 递归为CommentDTO添加children子评论
     *
     * @param commentIdToChildren 评论主键 -> 子评论集合的映射
     * @param commentDTO          需要添加子评论的评论
     * @param level               层级
     * @return 评论最大层级
     */
    private int setChildrenForCommentDto(
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
            int currentLevel = setChildrenForCommentDto(commentIdToChildren, dto, level + 1);
            maxLevel = Math.max(currentLevel, maxLevel);
        }
        return maxLevel;
    }

    @Override
    public IPage<CommentDTO> getFlatCommentsByOwnerId(long ownerId, long page, long size) {

        QueryWrapper<Comment> qw = new QueryWrapper<>();
        qw.eq("owner_id", ownerId);
        qw.eq("status", BlogConst.COMMENT_STATUS_APPROVED);
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
            QueryWrapper<Comment> parentCommentQw = new QueryWrapper<>();
            parentCommentQw.in("id", parentCommentIdSet);
            List<Comment> parentCommentList = baseMapper.selectList(parentCommentQw);
            parentCommentList.forEach(comment -> {
                Long id = comment.getId();
                parentIdToComment.put(id, comment);
            });
        }
        // 获取当前分页中评论的子评论
        List<Comment> childrenCommentList = new ArrayList<>();
        if (commentIdSet.size() > 0) {
            QueryWrapper<Comment> childrenCommentQw = new QueryWrapper<>();
            childrenCommentQw.in("parent_id", commentIdSet);
            childrenCommentList = baseMapper.selectList(childrenCommentQw);
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

        RestPage<CommentDTO> commentDtoPage = new RestPage<>(
                commentPage.getCurrent(),
                commentPage.getSize());
        commentDtoPage.setTotal(commentPage.getTotal());
        commentDtoPage.setRecords(commentDTOList);
        return commentDtoPage;
    }

    @Override
    public IPage<Comment> getCommentPage(String authorName,
                                         String email,
                                         String url,
                                         String content,
                                         String status,
                                         long page,
                                         long size) {
        QueryWrapper<Comment> qw = new QueryWrapper<>();
        qw.like(StringUtils.isNotBlank(authorName), "author_name", authorName);
        qw.like(StringUtils.isNotBlank(email), "email", email);
        qw.like(StringUtils.isNotBlank(url), "url", url);
        qw.like(StringUtils.isNotBlank(content), "content", content);
        qw.eq(StringUtils.isNotBlank(status), "status", status);
        qw.orderByDesc("create_time");

        return baseMapper.selectPage(new RestPage<>(page, size), qw);
    }

}
