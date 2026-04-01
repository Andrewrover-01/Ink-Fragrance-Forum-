package com.inkforum.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 返回给前端的帖子数据
 */
@Getter
@Setter
@Builder
public class PostResponse {

    private Long id;
    private String title;
    private String content;
    private String category;
    private String authorName;
    private Long authorId;
    private Long viewCount;
    private int commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
