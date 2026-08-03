package com.novel.controller;

import com.novel.common.Result;
import com.novel.dto.NovelCreateRequest;
import com.novel.dto.NovelUpdateRequest;
import com.novel.entity.Novel;
import com.novel.interceptor.JwtInterceptor;
import com.novel.service.AsyncTaskService;
import com.novel.service.NovelService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/novel")
@RequiredArgsConstructor
public class NovelController {

    private final NovelService novelService;
    private final AsyncTaskService asyncTaskService;

    @PostMapping
    public Result<Novel> create(@Valid @RequestBody NovelCreateRequest request,
                                HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute(JwtInterceptor.USER_ID_ATTR);
        return Result.success(novelService.createNovel(userId, request));
    }

    @PutMapping("/{id}")
    public Result<Novel> update(@PathVariable Long id,
                                @RequestBody NovelUpdateRequest request,
                                HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute(JwtInterceptor.USER_ID_ATTR);
        return Result.success(novelService.updateNovel(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute(JwtInterceptor.USER_ID_ATTR);
        novelService.deleteNovel(userId, id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<Novel> detail(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute(JwtInterceptor.USER_ID_ATTR);
        return Result.success(novelService.getNovelDetail(userId, id));
    }

    @GetMapping("/list")
    public Result<List<Novel>> list(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute(JwtInterceptor.USER_ID_ATTR);
        return Result.success(novelService.listUserNovels(userId));
    }

    @PostMapping("/{id}/generate-async")
    public Result<Void> generateAsync(@PathVariable Long id,
                                      @RequestBody Map<String, Object> body,
                                      HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute(JwtInterceptor.USER_ID_ATTR);
        novelService.getNovelDetail(userId, id);
        String prompt = (String) body.get("prompt");
        String genre = (String) body.get("genre");
        int targetWordCount = body.get("wordCount") != null ? (Integer) body.get("wordCount") : 10000;
        asyncTaskService.generateLongNovelAsync(id, prompt, genre, targetWordCount);
        return Result.success("长篇小说生成任务已提交", null);
    }

    @PostMapping("/{id}/export")
    public Result<Void> export(@PathVariable Long id,
                               @RequestParam(defaultValue = "txt") String format,
                               HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute(JwtInterceptor.USER_ID_ATTR);
        asyncTaskService.exportNovelAsync(id, format, userId);
        return Result.success("导出任务已提交", null);
    }
}
