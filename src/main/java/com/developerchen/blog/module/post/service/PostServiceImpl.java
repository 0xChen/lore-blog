package com.developerchen.blog.module.post.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.developerchen.blog.constant.BlogConst;
import com.developerchen.blog.module.category.domain.entity.Category;
import com.developerchen.blog.module.category.service.ICategoryService;
import com.developerchen.blog.module.post.common.PostReadEvent;
import com.developerchen.blog.module.post.domain.dto.Archive;
import com.developerchen.blog.module.post.domain.dto.PostDTO;
import com.developerchen.blog.module.post.domain.entity.Post;
import com.developerchen.blog.module.post.repository.PostMapper;
import com.developerchen.core.constant.Const;
import com.developerchen.core.domain.entity.User;
import com.developerchen.core.event.EntityCreateEvent;
import com.developerchen.core.event.EntityDeleteEvent;
import com.developerchen.core.service.IUserService;
import com.developerchen.core.service.impl.BaseServiceImpl;
import com.developerchen.core.domain.RestPage;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 文章表 服务实现类
 * </p>
 *
 * @author syc
 */
@Service
public class PostServiceImpl extends BaseServiceImpl<PostMapper, Post> implements IPostService {

    private final ICategoryService categoryService;

    private final IUserService userService;


    public PostServiceImpl(ICategoryService categoryService,
                           IUserService userService) {
        this.categoryService = categoryService;
        this.userService = userService;
    }

    /**
     * 根据类型获取文章列表
     *
     * @param mode 最新,随机
     * @param size 获取条数
     */
    @Override
    public List<Post> getPostList(String mode, long size) {
        // 查询条件
        QueryWrapper<Post> qw = new QueryWrapper<>();
        qw.eq("status", BlogConst.POST_STATUS_PUBLISH);
        qw.eq("type", BlogConst.POST_TYPE_POST);
        // 最新文章
        qw.orderByDesc(BlogConst.POST_RECENT.equals(mode), "pubdate");
        qw.orderByDesc(BlogConst.POST_RECENT.equals(mode), "create_time");
        // 随机文章
        qw.orderByAsc(BlogConst.POST_RANDOM.equals(mode), "rand()");

        IPage<Post> posts = baseMapper.selectPage(new Page<>(0, size), qw);
        return posts.getRecords();
    }

    /**
     * 查询文章归档
     */
    @Override
    public List<Archive> getArchiveList() {
        // 取所有发布状态的POST
        List<Post> postList = baseMapper.selectList(new QueryWrapper<Post>()
                .eq("status", BlogConst.POST_STATUS_PUBLISH));
        Map<String, Post> postIdToPost = postList.stream()
                .collect(Collectors.toMap(p -> p.getId().toString(), p -> p));

        // 按年月分组统计post数量并拼接post主键
        String querySql = new SQL()
                .SELECT("date_format(pubdate, '%Y年%m月') AS dateString",
                        "group_concat(id) AS postIds",
                        "count(*) AS count")
                .FROM("blog_post")
                .WHERE("status = '" + BlogConst.POST_STATUS_PUBLISH + "'")
                .WHERE("type = '" + BlogConst.POST_TYPE_POST + "'")
                .GROUP_BY("dateString")
                .ORDER_BY("dateString DESC")
                .toString();
        List<Archive> archiveList = baseMapper.selectListBySql(querySql, Archive.class);

        // 填充Archive中的postList属性
        archiveList.forEach(archive -> {
            String[] postIds = StringUtils.split(archive.getPostIds(), ",");
            archive.setPostIds(null);
            for (String postId : postIds) {
                Post post = postIdToPost.get(postId);
                archive.addPost(post);
            }
        });

        return archiveList;
    }

    /**
     * 获取指定状态和类型的文章数量
     *
     * @param status post状态
     * @param type   post类型
     * @return post数量
     */
    @Override
    public int postCount(String status, String type) {
        QueryWrapper<Post> qw = new QueryWrapper<>();
        qw.eq(status != null, "status", status);
        qw.eq(status != null, "type", type);
        return super.count(qw);
    }

    /**
     * 获取相邻的已发布状态的文章
     *
     * @param type    上一篇:prev | 下一篇:next
     * @param pubdate 当前文章发布时间
     */
    @Override
    public Post getPrevOrNextPost(String type, Date pubdate) {
        QueryWrapper<Post> qw = new QueryWrapper<>();
        qw.eq("status", BlogConst.POST_STATUS_PUBLISH);
        qw.eq("type", BlogConst.POST_TYPE_POST);
        qw.lt(Const.PAGE_PREV.equals(type), "pubdate", pubdate);
        qw.gt(Const.PAGE_NEXT.equals(type), "pubdate", pubdate);
        qw.orderByDesc(Const.PAGE_PREV.equals(type), "pubdate");
        qw.orderByAsc(Const.PAGE_NEXT.equals(type), "pubdate");
        qw.orderByDesc("create_time");

        IPage<Post> postPage = baseMapper.selectPage(new Page<>(0, 1), qw);
        List<Post> postList = postPage.getRecords();
        Post post = postList.size() > 0 ? postList.get(0) : null;

        if (post != null) {
            eventPublisher.publishEvent(new PostReadEvent<>(post));
        }

        return post;
    }

    /**
     * 分页形式获取文章
     *
     * @param title      文章标题查询条件
     * @param status     文章状态查询条件
     * @param type       内容类型查询条件
     * @param categoryId 当文章分类查询条件
     * @param page       当前页码
     * @param size       每页数量
     */
    @Override
    public IPage<Post> getPostPage(String title, String status, String type,
                                   Long categoryId, long page, long size) {
        type = StringUtils.isBlank(type) ? BlogConst.POST_TYPE_POST : type;
        QueryWrapper<Post> qw = new QueryWrapper<>();
        qw.like(StringUtils.isNotBlank(title), "title", title);
        qw.eq(StringUtils.isNotBlank(status), "status", status);
        qw.eq("type", type);
        qw.eq(categoryId != null, "categoryId", categoryId);
        qw.orderByDesc(BlogConst.POST_STATUS_PUBLISH.equals(status), "pubdate");
        qw.orderByDesc("create_time");
        IPage<Post> postPage = baseMapper.selectPage(new RestPage<>(page, size), qw);

        eventPublisher.publishEvent(new PostReadEvent<>(postPage));
        return postPage;
    }

    /**
     * 分页形式获取已发布状态文章
     *
     * @param page 当前页码
     * @param size 每页数量
     */
    @Override
    public IPage<Post> getPublishedPostPage(long page, long size) {
        QueryWrapper<Post> qw = new QueryWrapper<>();
        qw.eq("status", BlogConst.POST_STATUS_PUBLISH);
        qw.eq("type", BlogConst.POST_TYPE_POST);
        qw.orderByDesc("pubdate");
        qw.orderByDesc("create_time");
        IPage<Post> postPage = baseMapper.selectPage(new RestPage<>(page, size), qw);

        eventPublisher.publishEvent(new PostReadEvent<>(postPage));
        return postPage;
    }

    /**
     * 根据主键ID获取post
     *
     * @param postId post主键
     */
    @Override
    public PostDTO getPostDTObyId(long postId) {
        Post post = baseMapper.selectById(postId);
        Long userId = post.getAuthorId();
        Long categoryId = post.getCategoryId();

        User user = userService.getUserById(userId);
        Category category = categoryService.getCategoryById(categoryId);

        PostDTO postDTO = new PostDTO();
        BeanUtils.copyProperties(post, postDTO);
        postDTO.setAuthorName(user.getNickname());
        postDTO.setCategoryName(category.getName());

        return postDTO;
    }

    /**
     * 通过post的slug标识获取post
     *
     * @param slug slug标识
     */
    @Override
    public Post getPageBySlug(String slug) {
        Validate.notEmpty(slug, "slug不能为空");
        Post post = baseMapper.selectOne(new QueryWrapper<Post>().eq("slug", slug));

        if (post != null) {
            eventPublisher.publishEvent(new PostReadEvent<>(post));
        }
        return post;

    }

    /**
     * 分页形式获取指定分类下post
     *
     * @param categoryId 类别
     * @param page       当前页码
     * @param size       每页数量
     */
    @Override
    public IPage<Post> getPostPageByCategoryId(Long categoryId, long page, long size) {
        categoryId = categoryId == null ? BlogConst.CATEGORY_ROOT_ID : categoryId;
        QueryWrapper<Post> qw = new QueryWrapper<>();
        qw.eq("type", BlogConst.POST_TYPE_POST);
        qw.eq("category_Id", categoryId);
        qw.orderByDesc("pubdate");
        qw.orderByDesc("create_time");

        return baseMapper.selectPage(new RestPage<>(page, size), qw);
    }

    /**
     * 通过post的唯一name标识获取post, 并发布post被阅读的事件
     *
     * @param postIdOrSlug 主键ID或者name标识
     */
    @Override
    public Post getPostByIdOrSlug(String postIdOrSlug) {
        Validate.notEmpty(postIdOrSlug, "主键或者名称不能为空");
        Post post = null;
        // 如果不全是数字那么一定不是主键
        if (NumberUtils.isDigits(postIdOrSlug)) {
            post = baseMapper.selectById(postIdOrSlug);
        }
        // 全是数字的情况也不一定一定是主键
        if (post == null) {
            post = baseMapper.selectOne(new QueryWrapper<Post>().eq("slug", postIdOrSlug));
        }

        eventPublisher.publishEvent(new PostReadEvent<>(post));

        return post;
    }

    /**
     * 获取指定post
     *
     * @param postId post主键
     */
    @Override
    public Post getPostById(long postId) {
        return baseMapper.selectById(postId);
    }

    /**
     * 更新post
     * 如果post状态由[草稿] or [自动草稿] -> [发布]则同时更新post的发布日期
     */
    @Override
    public void updatePost(Post post) {
        if (BlogConst.POST_STATUS_PUBLISH.equals(post.getStatus())) {

            Post oldPost = baseMapper.selectById(post.getId());
            String prevStatus = oldPost.getStatus();
            if (BlogConst.POST_STATUS_DRAFT.equals(prevStatus)
                    || BlogConst.POST_STATUS_AUTO_DRAFT.equals(prevStatus)) {
                // 设置文章发布日期
                post.setPubdate(new Date());
            }
        }
        baseMapper.updateById(post);
    }

    /**
     * 更新post的阅读次数
     *
     * @param postId post主键
     * @param count  新增加的阅读次数
     */
    @Override
    public void increasePostReadCount(long postId, int count) {
        UpdateWrapper<Post> uw = new UpdateWrapper<>();
        uw.setSql("read_count = read_count + " + count).eq("id", postId);
        baseMapper.update(null, uw);
    }

    /**
     * 新增post，并发布Post新增事件
     */
    @Override
    public void savePost(Post post) {
        // 初始化评论数量
        post.setCommentCount(0);
        // 初始化阅读量
        post.setReadCount(0);
        if (BlogConst.POST_STATUS_PUBLISH.equals(post.getStatus())) {
            // 设置文章发布日期
            post.setPubdate(new Date());
        }
        // 没有指定分类则使用默认分类
        if (post.getCategoryId() == null) {
            post.setCategoryId(BlogConst.CATEGORY_ROOT_ID);
        }
        baseMapper.insert(post);
        eventPublisher.publishEvent(new EntityCreateEvent<>(post));
    }

    /**
     * 删除指定post, 并发布Post删除事件
     *
     * @param postId post主键
     */
    @Override
    public void deletePostById(long postId) {
        Post post = baseMapper.selectById(postId);
        // 发布Post删除事件
        eventPublisher.publishEvent(new EntityDeleteEvent<>(post));
        baseMapper.deleteById(postId);
    }

    /**
     * 增加Post的评论数量
     *
     * @param postId 增加评论数量的post主键
     */
    @Override
    public void increasePostCommentCount(long postId) {
        baseMapper.update(null, new UpdateWrapper<Post>()
                .setSql("comment_count = comment_count + 1")
                .eq("id", postId));
    }

    /**
     * 减少Post的评论数量
     *
     * @param postId 减少评论数量的post主键
     */
    @Override
    public void reducePostCommentCount(long postId) {
        baseMapper.update(null, new UpdateWrapper<Post>()
                .setSql("comment_count = comment_count - 1")
                .eq("id", postId));
    }

    /**
     * 获取用于构建站点地图的Post
     *
     * @return Post集合
     */
    @Override
    public List<Post> getPostForSitemap() {
        // 查询条件
        QueryWrapper<Post> qw = new QueryWrapper<>();
        qw.eq("status", BlogConst.POST_STATUS_PUBLISH);
        qw.eq("type", BlogConst.POST_TYPE_POST);
        // 排序
        qw.orderByDesc("pubdate");
        qw.orderByDesc("create_time");
        qw.orderByDesc("update_time");

        return baseMapper.selectList(qw);
    }

}
