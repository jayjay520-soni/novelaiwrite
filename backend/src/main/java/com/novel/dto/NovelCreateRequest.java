package com.novel.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NovelCreateRequest {

    @NotBlank(message = "标题不能为空")
    private String title;

    private String genre;

    private String summary;
}
