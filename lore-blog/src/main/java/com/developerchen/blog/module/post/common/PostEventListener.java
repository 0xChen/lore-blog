package com.developerchen.blog.module.post.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.developerchen.blog.module.post.domain.entity.Post;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * post阅读事件的监听器
 *
 * @author syc
 */
@Component
public class PostEventListener {

    /**
     * 记录没有持久化到数据库中的post的被阅读次数（相当于缓存post新增加的阅读次数）
     * post实际的阅读次数 = 当前Map中缓存的阅读次数 + 数据库中记录的当前post的阅读次数
     */
    private static final Map<Long, AtomicInteger> postReadCountCacheMap = new ConcurrentHashMap<>();


    /**
     * 记录并增加post的阅读次数
     *
     * @param postId post主键
     */
    public static void recordAndIncreaseCount(long postId) {
        AtomicInteger count;
        if (!postReadCountCacheMap.containsKey(postId)) {
            count = new AtomicInteger(0);
            postReadCountCacheMap.put(postId, count);
        } else {
            count = postReadCountCacheMap.get(postId);
        }
        count.getAndIncrement();
    }

    public static Map<Long, AtomicInteger> getPostReadCountCacheMap() {
        return PostEventListener.postReadCountCacheMap;
    }

    /**
     * 获取指定post的未持久化到数据库中的阅读次数
     *
     * @param postId 要获取阅读次数的post主键
     * @return post新增加的阅读次数
     */
    public static int getPostReadCount(long postId) {
        AtomicInteger count = postReadCountCacheMap.getOrDefault(postId,
                new AtomicInteger(0));
        return count.get();
    }

    /**
     * 当post被阅读时，增加post的阅读次数，该方法并不将增加的阅读次数更新到数据库中
     * 而是记录到缓存中。 之后由{@link PostReadCountTask}定时任务将阅读次数持久化
     * 到数据库中
     *
     * @param event post阅读事件
     */
    @Order(1)
    @EventListener
    public void increasePostReadCount(PostReadEvent<Post> event) {
        Post post = event.getSource();
        PostEventListener.recordAndIncreaseCount(post.getId());
    }

    /**
     * 更新当前查询的post中的阅读次数为最新的阅读次数
     * <p>service中读取的post中的阅读次数加上Map中缓存的阅读次数才是当前post
     * 的实际阅读次数。
     *
     * @param event post阅读事件
     */
    @Order(2)
    @EventListener
    public void addCachedPostReadCount(PostReadEvent<Post> event) {
        addCachedPostReadCount(event.getSource());
    }

    @EventListener
    public void addCachedPostReadCountForPostCollection(PostReadEvent<Collection<Post>> event) {
        event.getSource().forEach(this::addCachedPostReadCount);
    }

    @EventListener
    public void addCachedPostReadCountForPostPage(PostReadEvent<IPage<Post>> event) {
        event.getSource().getRecords().forEach(this::addCachedPostReadCount);
    }

    private void addCachedPostReadCount(Post post) {
        int cachedCount = PostEventListener.getPostReadCount(post.getId());
        post.setReadCount(post.getReadCount() + cachedCount);
    }

}
