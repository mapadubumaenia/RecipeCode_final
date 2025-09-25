package com.RecipeCode.teamproject.es.admin.service;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Log4j2
@SpringBootTest
class AdminAnalyticsServiceTest {

    @Autowired
    private AdminAnalyticsService adminAnalyticsService;

    @Test
    void topLiked() {
        int days=7;
        int size=5;

        List<Map<String, Object>> list= adminAnalyticsService.topLiked(days,size);

        log.info("test :" + list);

    }
}