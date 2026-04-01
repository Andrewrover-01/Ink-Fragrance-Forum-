package com.inkforum.repository;

import com.inkforum.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 帖子数据访问层
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    /** 按分类分页查询 */
    Page<Post> findByCategory(String category, Pageable pageable);

    /** 按标题关键字模糊查询（忽略大小写） */
    Page<Post> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);

    /** 按作者ID查询 */
    Page<Post> findByAuthorId(Long authorId, Pageable pageable);
}
