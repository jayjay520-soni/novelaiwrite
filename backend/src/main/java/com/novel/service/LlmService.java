package com.novel.service;

import com.novel.dto.LlmGenerateRequest;

public interface LlmService {

    void streamGenerate(String clientId, LlmGenerateRequest request, Long userId);
}
