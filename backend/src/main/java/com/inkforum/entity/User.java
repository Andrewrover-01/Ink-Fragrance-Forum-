package com.inkforum.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户实体 — represents a registered forum member.
 */
@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "email")
        })
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 用户名 */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 50, message = "用户名长度为2~50字符")
    @Column(nullable = false, length = 50)
    private String username;

    /** 邮箱 */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Column(nullable = false, length = 100)
    private String email;

    /** 密码（应存储哈希后的值） */
    @Column(nullable = false)
    private String password;

    /** 注册时间 */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 该用户发表的帖子 */
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    private List<Post> posts = new ArrayList<>();

    /** 该用户发表的评论 */
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
