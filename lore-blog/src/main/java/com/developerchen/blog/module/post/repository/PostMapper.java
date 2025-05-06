package com.developerchen.blog.module.post.repository;

import com.developerchen.blog.module.post.domain.dto.Archive;
import com.developerchen.blog.module.post.domain.entity.Post;
import com.developerchen.core.base.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 文章表 Mapper 接口
 * </p>
 *
 * @author syc
 */
public interface PostMapper extends BaseMapper<Post> {
    @Select("SELECT date_format(pubdate, '%Y年%m月') AS dateString, group_concat(id) AS postIds, count(*) AS count " +
            "FROM blog_post " +
            "WHERE status = #{status} AND type = #{type} " +
            "GROUP BY dateString " +
            "ORDER BY dateString DESC")
    List<Archive> selectArchiveList(@Param("status") String status, @Param("type") String type);
}
