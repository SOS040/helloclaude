package com.example.board.post.dto;

import java.time.LocalDateTime;

import com.example.board.post.domain.Post;

/**
 * 목록 조회용 응답 — 본문(content)은 제외한다.
 */
public record PostSummaryResponse(
		Long id,
		String title,
		String author,
		LocalDateTime createdAt) {

	public static PostSummaryResponse from(Post post) {
		return new PostSummaryResponse(post.getId(), post.getTitle(), post.getAuthor(), post.getCreatedAt());
	}

}
