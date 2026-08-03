package com.novel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.novel.dto.NovelCreateRequest;
import com.novel.dto.NovelUpdateRequest;
import com.novel.entity.Novel;

import java.util.List;

public interface NovelService extends IService<Novel> {

    Novel createNovel(Long userId, NovelCreateRequest request);

    Novel updateNovel(Long userId, Long novelId, NovelUpdateRequest request);

    void deleteNovel(Long userId, Long novelId);

    Novel getNovelDetail(Long userId, Long novelId);

    List<Novel> listUserNovels(Long userId);

    void appendContent(Long novelId, String content);
}
