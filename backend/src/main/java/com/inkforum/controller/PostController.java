package com.inkforum.controller;

import com.inkforum.dto.PostRequest;
import com.inkforum.dto.PostResponse;
import com.inkforum.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 帖子 RESTful API 控制器
 *
 * <pre>
 * POST   /api/posts            — 创建帖子
 * GET    /api/posts            — 分页获取所有帖子
 * GET    /api/posts/{id}       — 获取单篇帖子（含浏览次数递增）
 * PUT    /api/posts/{id}       — 更新帖子
 * DELETE /api/posts/{id}       — 删除帖子
 * GET    /api/posts/search     — 按标题关键字搜索
 * GET    /api/posts/category   — 按分类筛选
 * </pre>
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 创建帖子
     * POST /api/posts
     */
    @PostMapping
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody PostRequest request) {
        PostResponse created = postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 获取帖子列表（分页，默认按创建时间倒序）
     * GET /api/posts?page=0&size=10
     */
    @GetMapping
    public ResponseEntity<Page<PostResponse>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(postService.getAllPosts(pageable));
    }

    /**
     * 按 ID 获取帖子
     * GET /api/posts/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    /**
     * 更新帖子
     * PUT /api/posts/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostRequest request) {
        return ResponseEntity.ok(postService.updatePost(id, request));
    }

    /**
     * 删除帖子
     * DELETE /api/posts/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 搜索帖子（按标题关键字）
     * GET /api/posts/search?keyword=红楼&page=0&size=10
     */
    @GetMapping("/search")
    public ResponseEntity<Page<PostResponse>> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(postService.searchByTitle(keyword, pageable));
    }

    /**
     * 按分类筛选帖子
     * GET /api/posts/category?name=诗词鉴赏&page=0&size=10
     */
    @GetMapping("/category")
    public ResponseEntity<Page<PostResponse>> getByCategory(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(postService.getByCategory(name, pageable));
    }
}
