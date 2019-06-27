package com.developerchen.blog.module.comment.domain.dto;

import com.developerchen.blog.module.comment.domain.entity.Comment;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 评论表 数据传输对象
 * </p>
 *
 * @author syc
 */
public class CommentDTO extends Comment {

    private static final long serialVersionUID = 1L;

    /**
     * 层级
     */
    private Integer level;

    /**
     * 子评论
     */
    private List<CommentDTO> children;

    /**
     * 父评论
     */
    private Comment parent;

    public CommentDTO() {

    }

    public CommentDTO(Comment comment) {
        BeanUtils.copyProperties(comment, this);
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public List<CommentDTO> getChildren() {
        return children;
    }

    public void setChildren(List<CommentDTO> children) {
        this.children = children;
    }

    public void addChildren(List<CommentDTO> children) {
        this.children.addAll(children);
    }

    public void addChild(CommentDTO commentDTO) {
        if (children == null) {
            setChildren(new ArrayList<>());
        }
        this.children.add(commentDTO);
    }

    public Comment getParent() {
        return parent;
    }

    public void setParent(Comment parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
