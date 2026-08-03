package com.novel.controller;

import com.novel.common.Result;
import com.novel.dto.LlmGenerateRequest;
import com.novel.exception.BusinessException;
import com.novel.service.LlmService;
import com.novel.util.JwtUtil;
import com.novel.util.SseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/api/llm")
@RequiredArgsConstructor
public class LlmController {

    private final LlmService llmService;
    private final JwtUtil jwtUtil;

    @GetMapping(value = "/stream/{clientId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable String clientId,
                             @Valid @ModelAttribute LlmGenerateRequest request,
                             @RequestParam(required = false) String token) {
        if (!StringUtils.hasText(token) || !jwtUtil.validateToken(token)) {
            throw new BusinessException(401, "未登录或登录已过期");
        }
        Long userId = jwtUtil.getUserId(token);

        SseEmitter emitter = SseUtil.create(clientId);
        llmService.streamGenerate(clientId, request, userId);
        return emitter;
    }

    @PostMapping("/stream")
    public Result<String> createStreamSession(@Valid @RequestBody LlmGenerateRequest request) {
        String clientId = UUID.randomUUID().toString().replace("-", "");
        return Result.success(clientId);
    }
}
