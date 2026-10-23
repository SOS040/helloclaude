package com.example.board.post.controller;

import java.net.URI;

import com.example.board.global.common.PageResponse;
import com.example.board.post.dto.PostCreateRequest;
import com.example.board.post.dto.PostResponse;
import com.example.board.post.dto.PostSummaryResponse;
import com.example.board.post.dto.PostUpdateRequest;
import com.example.board.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

	private final PostService postService;

	@PostMapping
	public ResponseEntity<PostResponse> create(@Valid @RequestBody PostCreateRequest request) {
		PostResponse response = postService.create(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
			.path("/{id}")
			.buildAndExpand(response.id())
			.toUri();
		return ResponseEntity.created(location).body(response);
	}

	@GetMapping
	public PageResponse<PostSummaryResponse> getPosts(@RequestParam(required = false) String keyword,
			@PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
		return postService.getPosts(keyword, pageable);
	}

	@GetMapping("/{id}")
	public PostResponse getPost(@PathVariable Long id) {
		return postService.getPost(id);
	}

	@PutMapping("/{id}")
	public PostResponse update(@PathVariable Long id, @Valid @RequestBody PostUpdateRequest request) {
		return postService.update(id, request);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		postService.delete(id);
		return ResponseEntity.noContent().build();
	}

}
