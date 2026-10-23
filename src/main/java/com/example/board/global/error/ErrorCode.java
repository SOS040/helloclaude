package com.example.board.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	INVALID_SORT_PROPERTY(HttpStatus.BAD_REQUEST, "정렬할 수 없는 필드입니다."),

	POST_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다.");

	private final HttpStatus status;

	private final String message;

}
