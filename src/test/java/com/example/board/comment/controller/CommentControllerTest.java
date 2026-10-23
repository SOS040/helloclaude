package com.example.board.comment.controller;

import java.time.LocalDateTime;
import java.util.List;

import com.example.board.comment.dto.CommentCreateRequest;
import com.example.board.comment.dto.CommentResponse;
import com.example.board.comment.service.CommentService;
import com.example.board.global.common.PageResponse;
import com.example.board.global.error.BusinessException;
import com.example.board.global.error.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

@WebMvcTest(CommentController.class)
class CommentControllerTest {

	private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 23, 12, 0);

	@Autowired
	private MockMvcTester mvc;

	@MockitoBean
	private CommentService commentService;

	@Test
	void create() {
		given(commentService.create(eq(1L), any(CommentCreateRequest.class)))
			.willReturn(new CommentResponse(10L, 1L, "댓글", "댓글러", NOW, NOW));

		assertThat(mvc.post()
			.uri("/api/v1/posts/1/comments")
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
					{"content": "댓글", "author": "댓글러"}
					"""))
			.hasStatus(HttpStatus.CREATED)
			.hasHeader("Location", "http://localhost/api/v1/posts/1/comments/10")
			.bodyJson()
			.extractingPath("$.content")
			.isEqualTo("댓글");
	}

	@Test
	void createWithInvalidRequestReturnsProblemDetail() {
		assertThat(mvc.post()
			.uri("/api/v1/posts/1/comments")
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
					{"content": "", "author": "댓글러"}
					"""))
			.hasStatus(HttpStatus.BAD_REQUEST)
			.bodyJson()
			.extractingPath("$.errors[0].field")
			.isEqualTo("content");
	}

	@Test
	void createWhenPostNotFoundReturns404() {
		given(commentService.create(eq(99L), any(CommentCreateRequest.class)))
			.willThrow(new BusinessException(ErrorCode.POST_NOT_FOUND));

		assertThat(mvc.post()
			.uri("/api/v1/posts/99/comments")
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
					{"content": "댓글", "author": "댓글러"}
					"""))
			.hasStatus(HttpStatus.NOT_FOUND)
			.bodyJson()
			.extractingPath("$.code")
			.isEqualTo("POST_NOT_FOUND");
	}

	@Test
	void getComments() {
		PageResponse<CommentResponse> page = new PageResponse<>(
				List.of(new CommentResponse(10L, 1L, "댓글", "댓글러", NOW, NOW)), 0, 20, 1, 1, false);
		given(commentService.getComments(eq(1L), any(Pageable.class))).willReturn(page);

		assertThat(mvc.get().uri("/api/v1/posts/1/comments")).hasStatusOk()
			.bodyJson()
			.extractingPath("$.content[0].content")
			.isEqualTo("댓글");
	}

	@Test
	void update() {
		given(commentService.update(eq(1L), eq(10L), any()))
			.willReturn(new CommentResponse(10L, 1L, "수정된 댓글", "댓글러", NOW, NOW));

		assertThat(mvc.put()
			.uri("/api/v1/posts/1/comments/10")
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
					{"content": "수정된 댓글"}
					"""))
			.hasStatusOk()
			.bodyJson()
			.extractingPath("$.content")
			.isEqualTo("수정된 댓글");
	}

	@Test
	void delete() {
		assertThat(mvc.delete().uri("/api/v1/posts/1/comments/10")).hasStatus(HttpStatus.NO_CONTENT);
		then(commentService).should().delete(1L, 10L);
	}

	@Test
	void deleteWhenNotFoundReturns404() {
		willThrow(new BusinessException(ErrorCode.COMMENT_NOT_FOUND)).given(commentService).delete(1L, 99L);

		assertThat(mvc.delete().uri("/api/v1/posts/1/comments/99")).hasStatus(HttpStatus.NOT_FOUND)
			.bodyJson()
			.extractingPath("$.code")
			.isEqualTo("COMMENT_NOT_FOUND");
	}

}
