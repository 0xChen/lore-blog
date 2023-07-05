package com.developerchen.blog.module.post.domain.dto;

import com.developerchen.blog.module.post.domain.entity.Post;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 文章归档
 * <p>
 *
 * @author syc
 */
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
    private Date date;

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

    public String getDateString() {
        return dateString;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public List<Post> getPostList() {
        return postList;
    }

    public void setPostList(List<Post> postList) {
        this.postList = postList;
    }

    public void addPost(Post post) {
        this.postList.add(post);
    }

    public String getPostIds() {
        return postIds;
    }

    public void setPostIds(String postIds) {
        this.postIds = postIds;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
