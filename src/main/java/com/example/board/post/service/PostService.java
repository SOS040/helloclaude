package com.example.board.post.service;

import java.util.Set;

import com.example.board.global.common.PageResponse;
import com.example.board.global.error.BusinessException;
import com.example.board.global.error.ErrorCode;
import com.example.board.post.domain.Post;
import com.example.board.post.dto.PostCreateRequest;
import com.example.board.post.dto.PostResponse;
import com.example.board.post.dto.PostSummaryResponse;
import com.example.board.post.dto.PostUpdateRequest;
import com.example.board.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

	private static final Set<String> SORTABLE_PROPERTIES = Set.of("id", "title", "createdAt");

	private final PostRepository postRepository;

	@Transactional
	public PostResponse create(PostCreateRequest request) {
		Post post = Post.create(request.title(), request.content(), request.author());
		return PostResponse.from(postRepository.save(post));
	}

	public PageResponse<PostSummaryResponse> getPosts(String keyword, Pageable pageable) {
		validateSort(pageable.getSort());
		Page<Post> posts = StringUtils.hasText(keyword)
				? postRepository.findByTitleContainingIgnoreCase(keyword.strip(), pageable)
				: postRepository.findAll(pageable);
		return PageResponse.from(posts.map(PostSummaryResponse::from));
	}

	public PostResponse getPost(Long id) {
		return PostResponse.from(findPost(id));
	}

	@Transactional
	public PostResponse update(Long id, PostUpdateRequest request) {
		Post post = findPost(id);
		post.update(request.title(), request.content());
		// 응답에 갱신된 updatedAt을 담기 위해 커밋 전에 flush한다
		postRepository.flush();
		return PostResponse.from(post);
	}

	@Transactional
	public void delete(Long id) {
		postRepository.delete(findPost(id));
	}

	private Post findPost(Long id) {
		return postRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
	}

	private void validateSort(Sort sort) {
		for (Sort.Order order : sort) {
			if (!SORTABLE_PROPERTIES.contains(order.getProperty())) {
				throw new BusinessException(ErrorCode.INVALID_SORT_PROPERTY,
						"정렬할 수 없는 필드입니다: " + order.getProperty());
			}
		}
	}

}
