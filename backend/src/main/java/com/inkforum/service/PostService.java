package com.inkforum.service;

import com.inkforum.dto.PostRequest;
import com.inkforum.dto.PostResponse;
import com.inkforum.entity.Post;
import com.inkforum.entity.User;
import com.inkforum.repository.PostRepository;
import com.inkforum.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 帖子业务逻辑层
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // ─────────────────────────────────────────
    //  CREATE
    // ─────────────────────────────────────────

    /**
     * 创建新帖子
     */
    @Transactional
    public PostResponse createPost(PostRequest req) {
        User author = userRepository.findById(req.getAuthorId())
                .orElseThrow(() -> new EntityNotFoundException("用户不存在，ID=" + req.getAuthorId()));

        Post post = new Post();
        post.setTitle(req.getTitle());
        post.setContent(req.getContent());
        post.setCategory(req.getCategory());
        post.setAuthor(author);

        return toResponse(postRepository.save(post));
    }

    // ─────────────────────────────────────────
    //  READ
    // ─────────────────────────────────────────

    /**
     * 获取所有帖子（分页，按创建时间倒序）
     */
    public Page<PostResponse> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable).map(this::toResponse);
    }

    /**
     * 按 ID 获取帖子（同时递增浏览次数）
     */
    @Transactional
    public PostResponse getPostById(Long id) {
        Post post = findOrThrow(id);
        post.setViewCount(post.getViewCount() + 1);
        return toResponse(postRepository.save(post));
    }

    /**
     * 按关键字搜索帖子标题（分页）
     */
    public Page<PostResponse> searchByTitle(String keyword, Pageable pageable) {
        return postRepository.findByTitleContainingIgnoreCase(keyword, pageable)
                .map(this::toResponse);
    }

    /**
     * 按分类查询帖子（分页）
     */
    public Page<PostResponse> getByCategory(String category, Pageable pageable) {
        return postRepository.findByCategory(category, pageable).map(this::toResponse);
    }

    // ─────────────────────────────────────────
    //  UPDATE
    // ─────────────────────────────────────────

    /**
     * 更新帖子标题/内容/分类
     */
    @Transactional
    public PostResponse updatePost(Long id, PostRequest req) {
        Post post = findOrThrow(id);
        post.setTitle(req.getTitle());
        post.setContent(req.getContent());
        post.setCategory(req.getCategory());
        return toResponse(postRepository.save(post));
    }

    // ─────────────────────────────────────────
    //  DELETE
    // ─────────────────────────────────────────

    /**
     * 删除帖子
     */
    @Transactional
    public void deletePost(Long id) {
        Post post = findOrThrow(id);
        postRepository.delete(post);
    }

    // ─────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────

    private Post findOrThrow(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("帖子不存在，ID=" + id));
    }

    private PostResponse toResponse(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .category(post.getCategory())
                .authorName(post.getAuthor().getUsername())
                .authorId(post.getAuthor().getId())
                .viewCount(post.getViewCount())
                .commentCount(post.getComments().size())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
