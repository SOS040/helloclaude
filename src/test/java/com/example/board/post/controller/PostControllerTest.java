package com.example.board.post.controller;

import java.time.LocalDateTime;
import java.util.List;

import com.example.board.global.common.PageResponse;
import com.example.board.global.error.BusinessException;
import com.example.board.global.error.ErrorCode;
import com.example.board.post.dto.PostCreateRequest;
import com.example.board.post.dto.PostResponse;
import com.example.board.post.dto.PostSummaryResponse;
import com.example.board.post.service.PostService;
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

@WebMvcTest(PostController.class)
class PostControllerTest {

	private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 23, 12, 0);

	@Autowired
	private MockMvcTester mvc;

	@MockitoBean
	private PostService postService;

	@Test
	void create() {
		given(postService.create(any(PostCreateRequest.class)))
			.willReturn(new PostResponse(1L, "제목", "내용", "작성자", NOW, NOW));

		assertThat(mvc.post()
			.uri("/api/v1/posts")
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
					{"title": "제목", "content": "내용", "author": "작성자"}
					"""))
			.hasStatus(HttpStatus.CREATED)
			.hasHeader("Location", "http://localhost/api/v1/posts/1")
			.bodyJson()
			.extractingPath("$.title")
			.isEqualTo("제목");
	}

	@Test
	void createWithInvalidRequestReturnsProblemDetail() {
		assertThat(mvc.post()
			.uri("/api/v1/posts")
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
					{"title": " ", "content": "내용", "author": "작성자"}
					"""))
			.hasStatus(HttpStatus.BAD_REQUEST)
			.hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
			.bodyJson()
			.extractingPath("$.errors[0].field")
			.isEqualTo("title");
	}

	@Test
	void getPosts() {
		PageResponse<PostSummaryResponse> page = new PageResponse<>(
				List.of(new PostSummaryResponse(1L, "제목", "작성자", NOW)), 0, 20, 1, 1, false);
		given(postService.getPosts(eq("spring"), any(Pageable.class))).willReturn(page);

		assertThat(mvc.get().uri("/api/v1/posts").param("keyword", "spring"))
			.hasStatusOk()
			.bodyJson()
			.extractingPath("$.content[0].title")
			.isEqualTo("제목");
	}

	@Test
	void getPostWhenNotFoundReturns404() {
		given(postService.getPost(99L)).willThrow(new BusinessException(ErrorCode.POST_NOT_FOUND));

		assertThat(mvc.get().uri("/api/v1/posts/99")).hasStatus(HttpStatus.NOT_FOUND)
			.bodyJson()
			.extractingPath("$.code")
			.isEqualTo("POST_NOT_FOUND");
	}

	@Test
	void update() {
		given(postService.update(eq(1L), any())).willReturn(new PostResponse(1L, "새 제목", "새 내용", "작성자", NOW, NOW));

		assertThat(mvc.put()
			.uri("/api/v1/posts/1")
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
					{"title": "새 제목", "content": "새 내용"}
					"""))
			.hasStatusOk()
			.bodyJson()
			.extractingPath("$.title")
			.isEqualTo("새 제목");
	}

	@Test
	void delete() {
		assertThat(mvc.delete().uri("/api/v1/posts/1")).hasStatus(HttpStatus.NO_CONTENT);
		then(postService).should().delete(1L);
	}

	@Test
	void deleteWhenNotFoundReturns404() {
		willThrow(new BusinessException(ErrorCode.POST_NOT_FOUND)).given(postService).delete(99L);

		assertThat(mvc.delete().uri("/api/v1/posts/99")).hasStatus(HttpStatus.NOT_FOUND);
	}

}
