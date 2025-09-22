package com.RecipeCode.teamproject.reci.function.airpollution.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class AirService {

    private final RestTemplateBuilder builder;
    @Value("${air.base-url}")
    private String baseUrl;
    @Value("${air.service-key}")
    private String serviceKey;

    // 시도별 실시간 측정정보 조회
    public JsonNode getsidoRealtime(String sidoName) {
        RestTemplate rt = builder.build();

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

        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            throw new IllegalStateException("미세먼지 API 응답 오류");
        }
        return resp.getBody();
    }
}
