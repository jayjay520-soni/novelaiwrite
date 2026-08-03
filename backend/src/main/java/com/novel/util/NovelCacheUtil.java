package com.novel.util;

import com.novel.entity.Novel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class NovelCacheUtil {

    private static final String NOVEL_CACHE_PREFIX = "novel:cache:";
    private static final long CACHE_TTL_HOURS = 24;

    private final RedisUtil redisUtil;

    public void cacheNovel(Novel novel) {
        try {
            if (novel != null && novel.getId() != null) {
                redisUtil.set(NOVEL_CACHE_PREFIX + novel.getId(), novel, CACHE_TTL_HOURS, TimeUnit.HOURS);
            }
        } catch (Exception e) {
            log.warn("cacheNovel failed, novelId={}, error={}", novel != null ? novel.getId() : null, e.getMessage());
        }
    }

    public Novel getCachedNovel(Long novelId) {
        try {
            Object obj = redisUtil.get(NOVEL_CACHE_PREFIX + novelId);
            if (obj instanceof Novel novel) {
                return novel;
            }
        } catch (Exception e) {
            log.warn("getCachedNovel failed, novelId={}, error={}", novelId, e.getMessage());
        }
        return null;
    }

    public void evictNovel(Long novelId) {
        try {
            redisUtil.delete(NOVEL_CACHE_PREFIX + novelId);
        } catch (Exception e) {
            log.warn("evictNovel failed, novelId={}, error={}", novelId, e.getMessage());
        }
    }

    public void cacheUserNovelList(Long userId, Object novelList) {
        try {
            redisUtil.set(NOVEL_CACHE_PREFIX + "user:" + userId, novelList, CACHE_TTL_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("cacheUserNovelList failed, userId={}, error={}", userId, e.getMessage());
        }
    }

    public Object getCachedUserNovelList(Long userId) {
        try {
            return redisUtil.get(NOVEL_CACHE_PREFIX + "user:" + userId);
        } catch (Exception e) {
            log.warn("getCachedUserNovelList failed, userId={}, error={}", userId, e.getMessage());
            return null;
        }
    }

    public void evictUserNovelList(Long userId) {
        try {
            redisUtil.delete(NOVEL_CACHE_PREFIX + "user:" + userId);
        } catch (Exception e) {
            log.warn("evictUserNovelList failed, userId={}, error={}", userId, e.getMessage());
        }
    }
}
