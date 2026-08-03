package com.novel.service.impl;

import com.novel.entity.Novel;
import com.novel.service.AsyncTaskService;
import com.novel.service.NovelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncTaskServiceImpl implements AsyncTaskService {

    private static final String EXPORT_DIR = "exports";

    private final NovelService novelService;

    @Override
    @Async("novelTaskExecutor")
    public void generateLongNovelAsync(Long novelId, String prompt, String genre, int targetWordCount) {
        log.info("开始异步生成长篇小说: novelId={}, targetWords={}", novelId, targetWordCount);
        try {
            Novel novel = novelService.getById(novelId);
            if (novel == null) {
                return;
            }
            novel.setStatus(2);
            novelService.updateById(novel);

            // 分段生成：每段约2000字，直到达到目标字数
            int generated = 0;
            int segmentSize = 2000;
            StringBuilder fullContent = new StringBuilder(
                    novel.getContent() != null ? novel.getContent() : ""
            );

            while (generated < targetWordCount) {
                // 实际项目中此处调用 LlmService 非流式接口
                String segmentPrompt = prompt + "\n请继续创作下一段内容，约" + segmentSize + "字。";
                log.info("生成分段: novelId={}, progress={}/{}", novelId, generated, targetWordCount);
                // 占位：异步任务标记完成
                generated += segmentSize;
                if (generated >= targetWordCount) {
                    break;
                }
                Thread.sleep(1000);
            }

            novel.setContent(fullContent.toString());
            novel.setWordCount(fullContent.length());
            novel.setStatus(1);
            novelService.updateById(novel);
            log.info("长篇小说生成完成: novelId={}", novelId);
        } catch (Exception e) {
            log.error("长篇小说异步生成失败: novelId={}", novelId, e);
            Novel novel = novelService.getById(novelId);
            if (novel != null) {
                novel.setStatus(0);
                novelService.updateById(novel);
            }
        }
    }

    @Override
    @Async("novelTaskExecutor")
    public void exportNovelAsync(Long novelId, String format, Long userId) {
        log.info("开始异步导出: novelId={}, format={}", novelId, format);
        try {
            Novel novel = novelService.getById(novelId);
            if (novel == null || !novel.getUserId().equals(userId)) {
                return;
            }

            Path dir = Paths.get(EXPORT_DIR);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            String filename = novel.getTitle().replaceAll("[\\\\/:*?\"<>|]", "_") + "." + format;
            Path filePath = dir.resolve(userId + "_" + novelId + "_" + filename);

            String content = novel.getContent();
            if ("md".equalsIgnoreCase(format)) {
                content = "# " + novel.getTitle() + "\n\n" + content;
            }

            Files.writeString(filePath, content, StandardCharsets.UTF_8);
            log.info("导出完成: {}", filePath.toAbsolutePath());
        } catch (Exception e) {
            log.error("导出失败: novelId={}", novelId, e);
        }
    }
}
