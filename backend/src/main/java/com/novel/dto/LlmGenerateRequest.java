package com.novel.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LlmGenerateRequest {

    @NotBlank(message = "提示词不能为空")
    private String prompt;

    private String genre;

    private Integer wordCount;

    private Long novelId;

    private String systemPrompt;
}
