package com.developerchen.blog.module.post.domain.dto;

import com.developerchen.blog.module.post.domain.entity.Post;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 文章归档
 * <p>
 *
 * @author syc
 */
@Data
@Accessors(chain = true)
public class Archive implements Serializable {

    @Serial
    private static final long serialVersionUID = 6295126004938279284L;

    /**
     * 字符串形式归档日期, 格式为: '%Y年%m月
     */
    private String dateString;

    /**
     * 归档日期
     */
    private LocalDate date;

    /**
     * 归档数量
     */
    private String count;

    /**
     * 使用","连接的文章主键
     */
    private String postIds;

    /**
     * 文章集合
     */
    private List<Post> postList = new ArrayList<>();

    public void addPost(Post post) {
        this.postList.add(post);
    }

}
