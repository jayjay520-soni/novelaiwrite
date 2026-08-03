package com.novel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.novel.entity.SystemConfig;

import java.util.Map;

public interface SystemConfigService extends IService<SystemConfig> {

    String getConfigValue(String key);

    Map<String, String> getAllConfigs();

    void updateConfig(String key, String value);
}
