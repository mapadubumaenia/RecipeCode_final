package com.RecipeCode.teamproject.reci.function.airpollution.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AirService {

    private final RestTemplateBuilder builder;
    @Value("${air.base-url}")
    private String baseUrl;
    @Value("${air.service-key}")
    private String serviceKey;


    public JsonNode getsidoRealtime(String sidoName) {
        RestTemplate rt = builder
                .setConnectTimeout(java.time.Duration.ofSeconds(2))
                .setReadTimeout(java.time.Duration.ofSeconds(2))
                .build();

        try {
            URI uri = UriComponentsBuilder
                .fromHttpUrl(baseUrl+"/getCtprvnRltmMesureDnsty")
                .queryParam("serviceKey", serviceKey)
                .queryParam("sidoName", sidoName)
                .queryParam("returnType", "json")
                .queryParam("numOfRows", 100)
                .queryParam("pageNo", 1)
                .encode(StandardCharsets.UTF_8)
                .build().toUri();

            ResponseEntity<JsonNode> resp =
                rt.exchange(uri, HttpMethod.GET, null, JsonNode.class);

            // TODO: 1) HTTP 비정상 또는 body 없음 -> 폴백
            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                return buildStubResponse(sidoName, "http-non-2xx-or-null");
        }

            // TODO: 2) items 비었거나 구조 다름 -> 폴백
            JsonNode body =
                    resp.getBody().path("response").path("body").path("items");
            if (!body.isArray() || body.size() == 0) {
                return buildStubResponse(sidoName, "empty-or-invalid-items");
            }
            // TODO: 정상
            return resp.getBody();
        } catch (RestClientResponseException e) {
            // 4xx / 5xx 파싱 기능 예외
            return buildStubResponse(sidoName, "rest-client-ex");
        } catch (Exception e) {
            // 그 외 예외
            return buildStubResponse(sidoName, "generic-ex");
        }
    }

    // 공공 API 스펙(더미 데이터)
    public JsonNode buildStubResponse(String sidoName, String reason) {

        JsonNodeFactory f = JsonNodeFactory.instance;

        ObjectNode root  = f.objectNode();
        ObjectNode resp  = f.objectNode();
        ObjectNode body  = f.objectNode();
        ArrayNode items = f.arrayNode();

        // 시도별 기본값
        String name = (sidoName == null ? "" : sidoName.trim());

        // === 지역 그룹 정의 ===
        // 요청대로: 서울=매우나쁨 / 경기권=나쁨 / 중부=보통 / 남부=좋음
        List<String> veryBad = Arrays.asList("서울");
        List<String> bad     = Arrays.asList("경기", "인천");
        List<String> mid     = Arrays.asList("강원", "충북", "충남", "대전");
        List<String> good    = Arrays.asList(
                "부산", "대구", "울산", "광주",
                "전북", "전남", "경북", "경남", "제주", "세종"
        );

        // === 기본값(안전장치) ===
        int pm10Base = 38;  // 보통~약간좋음 사이
        int pm25Base = 17;

        if (veryBad.contains(name)) {
            // 매우나쁨: grade(pm10)=매우나쁨(>150), advice도 나쁨으로 떨어지게
            pm10Base = 200;
            pm25Base = 120;
        } else if (bad.contains(name)) {
            // 나쁨: pm10을 120(>80)로, pm2.5도 90(>80)로 줘서 grade/안내 모두 나쁨
            pm10Base = 120;
            pm25Base = 90;
        } else if (mid.contains(name)) {
            // 보통: 두 값 모두 31~80 구간
            pm10Base = 60;
            pm25Base = 45;
        } else if (good.contains(name)) {
            // 좋음: PM10 ≤ 30, PM2.5 ≤ 15
            pm10Base = 25;
            pm25Base = 12;
        } else {
            // 미지정 지역은 '보통'로 가정
            pm10Base = 60;
            pm25Base = 45;
        }

        // 시간 문자열 (프론트 표시용)
        String now = LocalDateTime.now()
                .withSecond(0)
                .withNano(0)
                .toString()
                .replace('T', ' ');

        // 측정소 5개 정도 생성해서 평균이 의미 있게 나오도록 약간의 변동치 부여
        for (int i = 0; i < 5; i++) {
            ObjectNode it = f.objectNode();
            it.put("dataTime", now);

            int pm10Val = pm10Base + ((i % 2 == 0) ? 0 : 1);            // 0/1 변동
            int pm25Val = pm25Base + ((i % 3 == 0) ? 0 : 1);            // 0/1 변동

            it.put("pm10Value", String.valueOf(pm10Val));
            it.put("pm25Value", String.valueOf(pm25Val));
            items.add(it);
        }

        body.set("items", items);
        resp.set("body", body);
        root.set("response", resp);

        // 디버깅/표시용 메타 (원하면 프론트에서 배지로 표시)
        root.put("_source", "stub");
        root.put("_reason", reason == null ? "" : reason);

        return root;
    }

    // 시도별 실시간 측정정보 조회(기존코드)
//    public JsonNode getsidoRealtime(String sidoName) {
//        RestTemplate rt = builder.build();
//
//        URI uri = UriComponentsBuilder
//                .fromHttpUrl(baseUrl+"/getCtprvnRltmMesureDnsty")
//                .queryParam("serviceKey", serviceKey)
//                .queryParam("sidoName", sidoName)
//                .queryParam("returnType", "json")
//                .queryParam("numOfRows", 100)
//                .queryParam("pageNo", 1)
//                .encode(StandardCharsets.UTF_8)
//                .build().toUri();
//
//        ResponseEntity<JsonNode> resp =
//                rt.exchange(uri, HttpMethod.GET, null, JsonNode.class);
//
//        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
//            throw new IllegalStateException("미세먼지 API 응답 오류");
//        }
//        return resp.getBody();
//    }
}
