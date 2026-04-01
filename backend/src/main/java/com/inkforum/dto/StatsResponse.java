package com.inkforum.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 论坛统计数据 DTO
 */
@Getter
@Setter
@Builder
public class StatsResponse {

    /** 注册会员总数 */
    private long memberCount;

    /** 帖子总数 */
    private long postCount;

    /** 评论总数 */
    private long commentCount;
}
