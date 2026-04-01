package com.inkforum.controller;

import com.inkforum.dto.StatsResponse;
import com.inkforum.repository.CommentRepository;
import com.inkforum.repository.PostRepository;
import com.inkforum.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页统计数据接口
 * GET /api/stats — 返回注册会员、帖子、评论总数
 */
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @GetMapping
    public ResponseEntity<StatsResponse> getStats() {
        StatsResponse stats = StatsResponse.builder()
                .memberCount(userRepository.count())
                .postCount(postRepository.count())
                .commentCount(commentRepository.count())
                .build();
        return ResponseEntity.ok(stats);
    }
}
