package com.RecipeCode.teamproject.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public final class CursorUtil {
    private CursorUtil() {}
    private static final ObjectMapper OM = new ObjectMapper();

    /** 권장: search_after 값을 JSON 배열로 직렬화해 Base64URL 인코딩(타입 보존) */
    public static String encode(List<Object> sortValues) {
        if (sortValues == null || sortValues.isEmpty()) return null;
        try {
            byte[] json = OM.writeValueAsBytes(sortValues);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(json);
        } catch (Exception e) {
            return null;
        }
    }

    /** 디코딩: 1) JSON 배열 우선 시도 2) 실패 시 기존 파이프 포맷 해석(레거시 호환) */
    public static List<Object> decode(String after) {
        if (after == null || after.isBlank()) return null;
        try {
            byte[] raw = Base64.getUrlDecoder().decode(after);

            // 1) JSON 배열 시도(타입 보존)
            try {
                return OM.readValue(raw, new TypeReference<List<Object>>() {});
            } catch (Exception ignore) {
                // continue to legacy
            }

            // 2) 레거시: "a|b|c" 포맷
            String s = new String(raw, StandardCharsets.UTF_8);
            List<Object> out = new ArrayList<>();
            for (String p : s.split("\\|", -1)) {
                if (p.equals("null") || p.isEmpty()) { out.add(null); continue; }
                if (p.equalsIgnoreCase("true") || p.equalsIgnoreCase("false")) { out.add(Boolean.parseBoolean(p)); continue; }
                if (p.matches("-?\\d+")) { out.add(Long.parseLong(p)); continue; }         // 정수
                if (p.matches("-?\\d+\\.\\d+")) { out.add(Double.parseDouble(p)); continue; } // 소수
                out.add(p); // 그 외(ISO-8601 날짜/키워드 등)
            }
            return out;
        } catch (Exception e) {
            return null;
        }
    }
}
