package com.inkforum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 创建/更新帖子时接收的请求体
 */
@Getter
@Setter
public class PostRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题不超过200字符")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    @Size(max = 50, message = "分类名称不超过50字符")
    private String category;

    @NotNull(message = "作者ID不能为空")
    private Long authorId;
}
