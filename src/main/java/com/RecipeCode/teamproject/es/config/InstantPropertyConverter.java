package com.RecipeCode.teamproject.es.config;

import org.springframework.data.elasticsearch.core.mapping.PropertyValueConverter;

import java.time.Instant;

/**
 * createdAt/updatedAt 전용 Instant 변환기
 */
public class InstantPropertyConverter implements PropertyValueConverter {

    @Override
    public Object write(Object value) {
        if (value == null) return null;
        // ES에 ISO8601 문자열("2025-09-15T00:00:00Z")로 저장
        return ((Instant) value).toString();
    }

    @Override
    public Object read(Object value) {
        if (value == null) return null;
        if (value instanceof String s) {
            // "2025-09-15T00:00:00Z" 같은 값 파싱
            return Instant.parse(s);
        }
        if (value instanceof Number n) {
            // epoch_millis 값이 들어올 경우
            return Instant.ofEpochMilli(n.longValue());
        }
        return Instant.parse(value.toString());
    }
}
