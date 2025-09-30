package com.RecipeCode.teamproject.es.reco.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventIngestService {

    @Value("${events.sink.enabled:true}")
    private boolean enabled;

    @Value("${events.sink.url:http://127.0.0.1:8081/}")
    private String sinkUrl;

    @Value("${events.sink.connectTimeoutMs:2000}")
    private int connectTimeoutMs;

    @Value("${events.sink.readTimeoutMs:2000}")
    private int readTimeoutMs;

    private final ObjectMapper om = new ObjectMapper();
    private RestTemplate rt;

    @PostConstruct
    void init() {
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        rt = new RestTemplate(factory);
        rt.setUriTemplateHandler(new DefaultUriBuilderFactory());
        log.info("[events] enabled={}, url={}, timeout(connect/read)={}ms/{}ms",
                enabled, sinkUrl, connectTimeoutMs, readTimeoutMs);
    }

    /**
     * 비동기 아니어도 충분히 빠름(로컬). 필요하면 @Async 붙여도 OK.
     * 모든 이메일은 소문자 강제, type은 view/like 소문자 그대로 보냄.
     */
    public void sendEvent(String userEmail, String recipeId, String type) {
        if (!enabled) {
            log.debug("[events] disabled: skip {} {} {}", userEmail, recipeId, type);
            return;
        }
        if (isBlank(userEmail) || isBlank(recipeId) || isBlank(type)) {
            log.warn("[events] invalid params: email={}, recipeId={}, type={}", userEmail, recipeId, type);
            return;
        }
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("userEmail", userEmail.trim().toLowerCase());
            payload.put("recipeId",  recipeId.trim());
            payload.put("type",      type.trim().toLowerCase());
            // eventTime 생략 시 Logstash 필터가 @timestamp now로 셋 ⇒ 굳이 안 넣어도 됨.

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> req = new HttpEntity<>(payload, headers);

            ResponseEntity<String> res = rt.postForEntity(sinkUrl, req, String.class);

            HttpStatusCode sc = res.getStatusCode();
            int code = sc.value();                     // 200
            String text = sc.toString();               // "200 OK" 같은 문자열

            log.debug("[events] POST {} -> {} ({}) body={}",
                    sinkUrl, code, text, res.getBody());
        } catch (Exception e) {
            log.warn("[events] send failed url={} msg={}", sinkUrl, e.toString());
        }
    }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
}
