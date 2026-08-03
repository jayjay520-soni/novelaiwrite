package com.novel.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SseUtil {

    private static final long DEFAULT_TIMEOUT = 30 * 60 * 1000L;
    private static final Map<String, SseEmitter> EMITTERS = new ConcurrentHashMap<>();

    private SseUtil() {}

    public static SseEmitter create(String clientId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        EMITTERS.put(clientId, emitter);

        emitter.onCompletion(() -> EMITTERS.remove(clientId));
        emitter.onTimeout(() -> {
            EMITTERS.remove(clientId);
            log.warn("SSE连接超时: {}", clientId);
        });
        emitter.onError(e -> {
            EMITTERS.remove(clientId);
            log.warn("SSE连接异常: {} - {}", clientId, e.getMessage());
        });

        return emitter;
    }

    public static void send(String clientId, String data) {
        SseEmitter emitter = EMITTERS.get(clientId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().data(data));
            } catch (IOException e) {
                EMITTERS.remove(clientId);
                log.error("SSE发送失败: {}", clientId, e);
            }
        }
    }

    public static void sendEvent(String clientId, String eventName, String data) {
        SseEmitter emitter = EMITTERS.get(clientId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (IOException e) {
                EMITTERS.remove(clientId);
                log.error("SSE事件发送失败: {}", clientId, e);
            }
        }
    }

    public static void complete(String clientId) {
        SseEmitter emitter = EMITTERS.remove(clientId);
        if (emitter != null) {
            emitter.complete();
        }
    }

    public static void completeWithError(String clientId, Throwable error) {
        SseEmitter emitter = EMITTERS.remove(clientId);
        if (emitter != null) {
            emitter.completeWithError(error);
        }
    }
}
