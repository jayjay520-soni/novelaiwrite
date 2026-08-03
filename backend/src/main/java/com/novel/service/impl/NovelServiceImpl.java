package com.novel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.novel.dto.NovelCreateRequest;
import com.novel.dto.NovelUpdateRequest;
import com.novel.entity.Novel;
import com.novel.exception.BusinessException;
import com.novel.mapper.NovelMapper;
import com.novel.service.NovelService;
import com.novel.util.NovelCacheUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NovelServiceImpl extends ServiceImpl<NovelMapper, Novel> implements NovelService {

    private final NovelCacheUtil novelCacheUtil;

    @Override
    public Novel createNovel(Long userId, NovelCreateRequest request) {
        Novel novel = new Novel();
        novel.setUserId(userId);
        novel.setTitle(request.getTitle());
        novel.setGenre(request.getGenre());
        novel.setSummary(request.getSummary());
        novel.setContent("");
        novel.setStatus(0);
        novel.setWordCount(0);
        save(novel);
        novelCacheUtil.evictUserNovelList(userId);
        return novel;
    }

    @Override
    public Novel updateNovel(Long userId, Long novelId, NovelUpdateRequest request) {
        Novel novel = getAndCheckOwnership(userId, novelId);
        if (StringUtils.hasText(request.getTitle())) {
            novel.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            novel.setContent(request.getContent());
            novel.setWordCount(request.getContent().length());
        }
        if (StringUtils.hasText(request.getGenre())) {
            novel.setGenre(request.getGenre());
        }
        if (request.getSummary() != null) {
            novel.setSummary(request.getSummary());
        }
        if (request.getStatus() != null) {
            novel.setStatus(request.getStatus());
        }
        updateById(novel);
        novelCacheUtil.cacheNovel(novel);
        novelCacheUtil.evictUserNovelList(userId);
        return novel;
    }

    @Override
    public void deleteNovel(Long userId, Long novelId) {
        getAndCheckOwnership(userId, novelId);
        removeById(novelId);
        novelCacheUtil.evictNovel(novelId);
        novelCacheUtil.evictUserNovelList(userId);
    }

    @Override
    public Novel getNovelDetail(Long userId, Long novelId) {
        Novel cached = novelCacheUtil.getCachedNovel(novelId);
        if (cached != null && cached.getUserId().equals(userId)) {
            return cached;
        }
        Novel novel = getAndCheckOwnership(userId, novelId);
        novelCacheUtil.cacheNovel(novel);
        return novel;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Novel> listUserNovels(Long userId) {
        Object cached = novelCacheUtil.getCachedUserNovelList(userId);
        if (cached instanceof List) {
            return (List<Novel>) cached;
        }
        List<Novel> list = list(new LambdaQueryWrapper<Novel>()
                .eq(Novel::getUserId, userId)
                .orderByDesc(Novel::getUpdateTime));
        novelCacheUtil.cacheUserNovelList(userId, list);
        return list;
    }

    @Override
    public void appendContent(Long novelId, String content) {
        Novel novel = getById(novelId);
        if (novel == null) {
            return;
        }
        String existing = novel.getContent() != null ? novel.getContent() : "";
        novel.setContent(existing + content);
        novel.setWordCount(novel.getContent().length());
        novel.setStatus(2);
        updateById(novel);
        novelCacheUtil.cacheNovel(novel);
    }

    private Novel getAndCheckOwnership(Long userId, Long novelId) {
        Novel novel = getById(novelId);
        if (novel == null) {
            throw new BusinessException("作品不存在");
        }
        if (!novel.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权访问该作品");
        }
        return novel;
    }
}
