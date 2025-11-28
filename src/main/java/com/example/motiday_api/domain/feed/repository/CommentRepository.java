package com.example.motiday_api.domain.feed.repository;

import com.example.motiday_api.domain.feed.entity.Comment;
import com.example.motiday_api.domain.feed.entity.Feed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 피드별 댓글 목록
    List<Comment> findByFeedOrderByCreatedAtAsc(Feed feed);

    // 피드별 댓글 수
    int countByFeed(Feed feed);
}