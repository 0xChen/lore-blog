package com.developerchen.blog.module.comment.domain.dto;

import com.developerchen.blog.module.comment.domain.entity.Comment;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 评论表 数据传输对象
 * </p>
 *
 * @author syc
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class CommentDTO extends Comment {

    @Serial
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


    public void addChildren(List<CommentDTO> children) {
        this.children.addAll(children);
    }

    public void addChild(CommentDTO commentDTO) {
        if (children == null) {
            setChildren(new ArrayList<>());
        }
        this.children.add(commentDTO);
    }

}
