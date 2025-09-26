package com.RecipeCode.teamproject.reci.function.airpollution;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SidoResolver {
    private static final Map<String, String> M = Map.ofEntries(
            Map.entry("seoul", "서울"),
            Map.entry("busan", "부산"),
            Map.entry("daegu", "대구"),
            Map.entry("incheon", "인천"),
            Map.entry("gwangju", "광주"),
            Map.entry("daejeon", "대전"),
            Map.entry("ulsan", "울산"),
            Map.entry("sejong", "세종"),
            Map.entry("gg", "경기"),
            Map.entry("gyeonggi", "경기"), Map.entry("gyeonggi-do", "경기"),
            Map.entry("gangwon", "강원"),   Map.entry("gangwon-do", "강원"),
            Map.entry("chungbuk", "충북"),  Map.entry("chungcheongbuk", "충북"),
            Map.entry("chungnam", "충남"),  Map.entry("chungcheongnam", "충남"),
            Map.entry("jeonbuk", "전북"),   Map.entry("jeollabuk", "전북"),
            Map.entry("jeonnam", "전남"),   Map.entry("jeollanam", "전남"),
            Map.entry("gyeongbuk", "경북"), Map.entry("gyeongsangbuk", "경북"),
            Map.entry("gyeongnam", "경남"), Map.entry("gyeongsangnam", "경남"),
            Map.entry("jeju", "제주"),      Map.entry("jeju-do", "제주")
    );

    public String resolve(String raw, String fallback){
        if(raw == null || raw.isBlank()) return fallback;
        String head = raw.split(",")[0].trim(); // , 기준으로 1번째 Busan, KR = Busan
        String key  = head.toLowerCase()
                .replaceAll("\\s+","")
                .replaceAll("-do$","")
                .replaceAll("province","");

        // 한글로 이미 들어오면 그대로 쓰고, 영어면 매핑
        if (M.containsKey(key)) return M.get(key);

        // 이미 한글일 때 특별시 광역시 도 제거
        String korean = head.replace("특별시", "")
                .replace("광역시", "")
                .replace("도", "")
                .trim();
        if (!korean.isBlank()) return korean;

        return fallback;
    }

}
