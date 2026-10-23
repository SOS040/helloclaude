package com.example.board.post.repository;

import com.example.board.global.config.JpaAuditingConfig;
import com.example.board.post.domain.Post;
import com.example.board.support.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({ TestcontainersConfiguration.class, JpaAuditingConfig.class })
@ActiveProfiles("test")
class PostRepositoryTest {

	@Autowired
	private PostRepository postRepository;

	@Test
	void saveSetsIdAndAuditingFields() {
		Post saved = postRepository.save(Post.create("제목", "내용", "작성자"));

		assertThat(saved.getId()).isNotNull();
		assertThat(saved.getCreatedAt()).isNotNull();
		assertThat(saved.getUpdatedAt()).isNotNull();
	}

	@Test
	void findByTitleContainingIgnoreCase() {
		postRepository.save(Post.create("Spring Boot 4", "내용", "작성자"));
		postRepository.save(Post.create("spring data jpa", "내용", "작성자"));
		postRepository.save(Post.create("PostgreSQL 18", "내용", "작성자"));

		Page<Post> page = postRepository.findByTitleContainingIgnoreCase("SPRING",
				PageRequest.of(0, 10, Sort.by("id").descending()));

		assertThat(page.getTotalElements()).isEqualTo(2);
		assertThat(page.getContent()).extracting(Post::getTitle)
			.containsExactly("spring data jpa", "Spring Boot 4");
	}

}
