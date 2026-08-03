package com.novel.controller;

import com.novel.common.Result;
import com.novel.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @GetMapping
    public Result<Map<String, String>> getAllConfigs() {
        return Result.success(systemConfigService.getAllConfigs());
    }

    @GetMapping("/{key}")
    public Result<String> getConfig(@PathVariable String key) {
        return Result.success(systemConfigService.getConfigValue(key));
    }

    @PutMapping("/{key}")
    public Result<Void> updateConfig(@PathVariable String key, @RequestBody Map<String, String> body) {
        systemConfigService.updateConfig(key, body.get("value"));
        return Result.success();
    }
}
