package com.novel.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novel.config.LlmProperties;
import com.novel.dto.LlmGenerateRequest;
import com.novel.service.LlmService;
import com.novel.service.NovelService;
import com.novel.util.SseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmServiceImpl implements LlmService {

    private final LlmProperties llmProperties;
    private final NovelService novelService;
    private final ObjectMapper objectMapper;

    @Override
    public void streamGenerate(String clientId, LlmGenerateRequest request, Long userId) {
        String systemPrompt = StringUtils.hasText(request.getSystemPrompt())
                ? request.getSystemPrompt()
                : buildDefaultSystemPrompt(request);

        Map<String, Object> body = new HashMap<>();
        body.put("model", llmProperties.getModel());
        body.put("stream", true);
        body.put("max_tokens", llmProperties.getMaxTokens());
        body.put("temperature", llmProperties.getTemperature());
        body.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", request.getPrompt())
        ));

        StringBuilder fullContent = new StringBuilder();

        WebClient.create()
                .post()
                .uri(llmProperties.getApiUrl())
                .header("Authorization", "Bearer " + llmProperties.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .onErrorResume(e -> {
                    log.error("大模型API调用失败", e);
                    SseUtil.sendEvent(clientId, "error", "大模型服务暂时不可用，请稍后重试");
                    SseUtil.complete(clientId);
                    return Flux.empty();
                })
                .subscribe(
                        chunk -> processChunk(clientId, chunk, fullContent, request),
                        error -> {
                            log.error("流式生成异常", error);
                            SseUtil.sendEvent(clientId, "error", error.getMessage());
                            SseUtil.complete(clientId);
                        },
                        () -> {
                            if (request.getNovelId() != null && fullContent.length() > 0) {
                                novelService.appendContent(request.getNovelId(), fullContent.toString());
                            }
                            SseUtil.sendEvent(clientId, "done", "[DONE]");
                            SseUtil.complete(clientId);
                        }
                );
    }

    private void processChunk(String clientId, String chunk, StringBuilder fullContent, LlmGenerateRequest request) {
        if ("[DONE]".equals(chunk.trim())) {
            return;
        }
        try {
            JsonNode node = objectMapper.readTree(chunk);
            JsonNode choices = node.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                JsonNode delta = choices.get(0).path("delta");
                String content = delta.path("content").asText("");
                if (StringUtils.hasText(content)) {
                    fullContent.append(content);
                    SseUtil.send(clientId, content);
                }
            }
        } catch (Exception e) {
            // SSE data lines may come prefixed with "data: "
            String line = chunk.startsWith("data: ") ? chunk.substring(6).trim() : chunk.trim();
            if ("[DONE]".equals(line) || line.isEmpty()) {
                return;
            }
            try {
                JsonNode node = objectMapper.readTree(line);
                JsonNode choices = node.path("choices");
                if (choices.isArray() && !choices.isEmpty()) {
                    String content = choices.get(0).path("delta").path("content").asText("");
                    if (StringUtils.hasText(content)) {
                        fullContent.append(content);
                        SseUtil.send(clientId, content);
                    }
                }
            } catch (Exception ignored) {
                log.debug("跳过无法解析的SSE块: {}", line);
            }
        }
    }

    private String buildDefaultSystemPrompt(LlmGenerateRequest request) {
        String genre = StringUtils.hasText(request.getGenre()) ? request.getGenre() : "通用";
        int wordCount = request.getWordCount() != null ? request.getWordCount() : 2000;
        return String.format(
                "你是一位专业的%s小说作家。请根据用户提供的创意和要求，创作高质量的小说内容。" +
                "要求：语言流畅、情节引人入胜、人物刻画生动。目标字数约%d字。直接输出小说正文，不要额外解释。",
                genre, wordCount
        );
    }
}
