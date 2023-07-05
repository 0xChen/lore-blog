package com.developerchen.blog.module.post.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.developerchen.blog.constant.BlogConst;
import com.developerchen.blog.exception.BlogException;
import com.developerchen.blog.module.category.domain.entity.Category;
import com.developerchen.blog.module.category.service.ICategoryService;
import com.developerchen.blog.module.post.common.PostReadEvent;
import com.developerchen.blog.module.post.domain.dto.Archive;
import com.developerchen.blog.module.post.domain.dto.PostDTO;
import com.developerchen.blog.module.post.domain.entity.Post;
import com.developerchen.blog.module.post.repository.PostMapper;
import com.developerchen.core.constant.Const;
import com.developerchen.core.domain.RestPage;
import com.developerchen.core.domain.entity.User;
import com.developerchen.core.event.EntityCreateEvent;
import com.developerchen.core.event.EntityDeleteEvent;
import com.developerchen.core.service.IUserService;
import com.developerchen.core.service.impl.BaseServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Override
    public Long countPost(String status, String type) {
        QueryWrapper<Post> qw = new QueryWrapper<>();
        qw.eq(status != null, "status", status);
        qw.eq(status != null, "type", type);
        return super.count(qw);
    }

    @Override
    public Long countTag() {
        QueryWrapper<Post> qw = new QueryWrapper<>();
        qw.select("tags").isNotNull("tags").ne("tags", "");

        List<Post> postList = baseMapper.selectList(qw);

        return postList.stream().filter(post -> StringUtils.isNotEmpty(post.getTags()))
                .flatMap(post -> Stream.of(post.getTags().split(",")))
                .map(String::trim).distinct().count();
    }

    @Override
    public Set<String> getTags() {
        QueryWrapper<Post> qw = new QueryWrapper<>();
        //  如果数据库 tags 为 null 时,
        //  将导致集合中放入 null 而不是一个没有任何值的 post 实例 ,
        //  所以一定要加入排空条件或者加入 id 这种一定有值的字段一起 select
        //  否则查询后代码中一定要做 null 验证
        qw.select("tags").isNotNull("tags").ne("tags", "");
        List<Post> postList = baseMapper.selectList(qw);
        return postList.stream().filter(post -> StringUtils.isNotEmpty(post.getTags()))
                .flatMap(post -> Stream.of(post.getTags().split(",")))
                .map(String::trim).collect(Collectors.toSet());
    }

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

    @Override
    public IPage<PostDTO> getPostPage(String title, String status, String type, String tags,
                                      Long categoryId, long page, long size) {
        type = StringUtils.isBlank(type) ? BlogConst.POST_TYPE_POST : type;
        QueryWrapper<Post> qw = new QueryWrapper<>();
        qw.like(StringUtils.isNotBlank(title), "title", title);
        qw.eq(StringUtils.isNotBlank(status), "status", status);
        qw.eq("type", type);
        qw.like(StringUtils.isNotBlank(tags), "tags", tags);
        qw.eq(categoryId != null, "categoryId", categoryId);
        qw.orderByDesc(BlogConst.POST_STATUS_PUBLISH.equals(status), "pubdate");
        qw.orderByDesc("create_time");
        IPage<Post> postPage = baseMapper.selectPage(new RestPage<>(page, size), qw);

        // 分类数据量不会很大, 直接获取全部
        List<Category> categoryList = categoryService.getAllCategory();
        Map<Long, String> categoryIdToName = categoryList.stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        List<PostDTO> postDtoList = postPage.getRecords().stream().map(post -> {
            PostDTO postDto = new PostDTO();
            BeanUtils.copyProperties(post, postDto);
            postDto.setCategoryName(categoryIdToName.get(post.getCategoryId()));
            return postDto;
        }).collect(Collectors.toList());

        RestPage<PostDTO> postDtoPage = new RestPage<>(
                postPage.getCurrent(),
                postPage.getSize());
        postDtoPage.setTotal(postPage.getTotal());
        postDtoPage.setRecords(postDtoList);

        eventPublisher.publishEvent(new PostReadEvent<>(postPage));
        return postDtoPage;
    }

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

    @Override
    public PostDTO getPostDtoById(long postId) {
        Post post = baseMapper.selectById(postId);
        Long userId = post.getAuthorId();
        Long categoryId = post.getCategoryId();

        User user = userService.getUserById(userId);
        Category category = categoryService.getCategoryById(categoryId);

        PostDTO postDto = new PostDTO();
        BeanUtils.copyProperties(post, postDto);
        postDto.setAuthorName(user.getNickname());
        postDto.setCategoryName(category.getName());

        return postDto;
    }

    @Override
    public Post getPageBySlug(String slug) {
        Validate.notEmpty(slug, "slug不能为空");
        Post post = baseMapper.selectOne(new QueryWrapper<Post>().eq("slug", slug));

        if (post != null) {
            eventPublisher.publishEvent(new PostReadEvent<>(post));
        }
        return post;

    }

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

    @Override
    public IPage<Post> getPostPageByTag(String tagName, long page, long size) {
        QueryWrapper<Post> qw = new QueryWrapper<>();
        qw.eq("type", BlogConst.POST_TYPE_POST);
        qw.and(c -> c.eq("tags", tagName)
                .or().like("tags", "," + tagName + ",")
                .or().likeLeft("tags", "," + tagName)
                .or().likeRight("tags", tagName + ","));

        qw.orderByDesc("pubdate");
        qw.orderByDesc("create_time");

        return baseMapper.selectPage(new RestPage<>(page, size), qw);
    }

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

        if (post != null) {
            eventPublisher.publishEvent(new PostReadEvent<>(post));
        }

        return post;
    }

    @Override
    public Post getPostById(long postId) {
        return baseMapper.selectById(postId);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
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

    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void updateStatusByPostId(long id, String status) {
        Post post = new Post();
        post.setId(id);
        post.setStatus(status);
        updatePost(post);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void increasePostReadCount(long postId, int count) {
        UpdateWrapper<Post> uw = new UpdateWrapper<>();
        uw.setSql("read_count = read_count + " + count).eq("id", postId);
        baseMapper.update(null, uw);
    }


    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
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

    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void deletePostById(long postId) {
        Post post = baseMapper.selectById(postId);
        // 发布Post删除事件
        eventPublisher.publishEvent(new EntityDeleteEvent<>(post));
        baseMapper.deleteById(postId);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void deletePostByIds(Set<Long> postIds) {
        List<Post> postList = baseMapper.selectBatchIds(postIds);
        if (postList.size() != postIds.size()) {
            throw new BlogException("删除失败, 因某些文章或页面已不存在, 请刷新页面后重试. ");
        }
        if (postList.size() > 0) {
            baseMapper.deleteBatchIds(postIds);
            eventPublisher.publishEvent(new EntityDeleteEvent<>(postList));
        }
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void increasePostCommentCount(long postId) {
        baseMapper.update(null, new UpdateWrapper<Post>()
                .setSql("comment_count = comment_count + 1")
                .eq("id", postId));
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void reducePostCommentCount(long postId) {
        baseMapper.update(null, new UpdateWrapper<Post>()
                .setSql("comment_count = comment_count - 1")
                .eq("id", postId));
    }

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
