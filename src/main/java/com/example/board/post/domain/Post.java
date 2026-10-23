package com.example.board.post.domain;

import com.example.board.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "post")
public class Post extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 200)
	private String title;

	@Column(nullable = false, columnDefinition = "text")
	private String content;

	@Column(nullable = false, length = 50)
	private String author;

	private Post(String title, String content, String author) {
		this.title = title;
		this.content = content;
		this.author = author;
	}

	public static Post create(String title, String content, String author) {
		return new Post(title, content, author);
	}

	public void update(String title, String content) {
		this.title = title;
		this.content = content;
	}

}
