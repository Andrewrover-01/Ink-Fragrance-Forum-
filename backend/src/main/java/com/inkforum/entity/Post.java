package com.inkforum.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 帖子实体 — represents a forum post.
 */
@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 标题 */
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题不超过200字符")
    @Column(nullable = false, length = 200)
    private String title;

    /** 正文内容 */
    @NotBlank(message = "内容不能为空")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 所属分类（如：诗词鉴赏、小说推荐、历史典故…） */
    @Column(length = 50)
    private String category;

    /** 作者 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    /** 浏览次数 */
    @Column(nullable = false)
    private Long viewCount = 0L;

    /** 创建时间 */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 最后更新时间 */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /** 评论列表 */
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
