package com.novel.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novel.common.Result;
import com.novel.common.ResultCode;
import com.novel.config.RateLimitProperties;
import com.novel.util.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    private final RateLimitProperties rateLimitProperties;
    private final RedisUtil redisUtil;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!rateLimitProperties.isEnabled() || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        try {
            String clientKey = resolveClientKey(request);
            String redisKey = RATE_LIMIT_PREFIX + clientKey;

            Long count = redisUtil.increment(redisKey);
            if (count == null) {
                // Redis 不可用，降级为不限流，放行
                return true;
            }
            if (count == 1) {
                redisUtil.expire(redisKey, rateLimitProperties.getWindowSeconds(), TimeUnit.SECONDS);
            }

            if (count > rateLimitProperties.getMaxRequests()) {
                response.setStatus(429);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(objectMapper.writeValueAsString(Result.fail(ResultCode.RATE_LIMIT)));
                return false;
            }
        } catch (Exception e) {
            // 任何限流相关异常都不阻断正常请求，降级放行
            return true;
        }
        return true;
    }

    private String resolveClientKey(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (StringUtils.hasText(auth)) {
            return auth;
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (!StringUtils.hasText(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip + ":" + request.getRequestURI();
    }
}
