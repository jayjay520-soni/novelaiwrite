package com.novel.service;

public interface AsyncTaskService {

    void generateLongNovelAsync(Long novelId, String prompt, String genre, int targetWordCount);

    void exportNovelAsync(Long novelId, String format, Long userId);
}
