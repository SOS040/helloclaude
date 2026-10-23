package com.example.board.global.common;

import java.util.Set;

import com.example.board.global.error.BusinessException;
import com.example.board.global.error.ErrorCode;
import org.springframework.data.domain.Sort;

/**
 * 클라이언트가 보낸 sort 파라미터를 허용된 필드로만 제한한다.
 */
public final class SortValidator {

	private SortValidator() {
	}

	public static void validate(Sort sort, Set<String> sortableProperties) {
		for (Sort.Order order : sort) {
			if (!sortableProperties.contains(order.getProperty())) {
				throw new BusinessException(ErrorCode.INVALID_SORT_PROPERTY,
						"정렬할 수 없는 필드입니다: " + order.getProperty());
			}
		}
	}

}
