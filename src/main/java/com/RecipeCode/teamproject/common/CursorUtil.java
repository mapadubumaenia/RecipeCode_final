package com.RecipeCode.teamproject.common;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public final class CursorUtil {
    private CursorUtil() {}

    public static String encode(List<Object> sortValues) {
        String joined = sortValues.stream().map(String::valueOf)
                .reduce((a,b)->a + "|" + b).orElse("");
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(joined.getBytes(StandardCharsets.UTF_8));
    }

    public static List<Object> decode(String after) {
        if (after == null || after.isBlank()) return null;
        String s = new String(Base64.getUrlDecoder().decode(after), StandardCharsets.UTF_8);
        var out = new ArrayList<Object>();
        for (var p : s.split("\\|", -1)) {
            if (p.matches("-?\\d+")) out.add(Long.parseLong(p)); // 숫자면 Long
            else out.add(p);                                     // 그 외(ISO 날짜 등)
        }
        return out;
    }
}