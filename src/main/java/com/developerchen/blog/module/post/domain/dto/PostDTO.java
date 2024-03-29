package com.developerchen.blog.module.post.domain.dto;

import com.developerchen.blog.module.post.domain.entity.Post;

import java.io.Serial;

/**
 * Post数据传输对象
 *
 * @author syc
 */
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


    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
