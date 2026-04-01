package com.inkforum.repository;

import com.inkforum.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 评论数据访问层
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /** 查询某帖子下的所有评论（分页） */
    Page<Comment> findByPostId(Long postId, Pageable pageable);

    /** 查询某用户的所有评论 */
    Page<Comment> findByAuthorId(Long authorId, Pageable pageable);
}
