package com.developerchen.blog.module.post.domain.dto;

import com.developerchen.blog.module.post.domain.entity.Post;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * Post数据传输对象
 *
 * @author syc
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PostDTO extends Post {

    @Serial
    private static final long serialVersionUID = -9028464940628426300L;

    /**
     * 文章作者
     */
    private String authorName;

    /**
     * 分类名称
     */
    private String categoryName;
}
