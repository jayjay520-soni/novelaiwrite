package com.novel.dto;

import lombok.Data;

@Data
public class NovelUpdateRequest {

    private String title;
    private String content;
    private String genre;
    private String summary;
    private Integer status;
}
