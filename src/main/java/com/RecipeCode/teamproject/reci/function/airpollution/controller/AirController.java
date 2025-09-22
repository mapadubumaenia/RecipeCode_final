package com.RecipeCode.teamproject.reci.function.airpollution.controller;

import com.RecipeCode.teamproject.reci.function.airpollution.service.AirService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/air")
@RequiredArgsConstructor
public class AirController {
    private final AirService airService;

    @GetMapping("/now")
    public ResponseEntity<JsonNode> now(@RequestParam(required = false) String sido) {
        JsonNode root = airService.getsidoRealtime(sido);
        return ResponseEntity.ok(root);
    }
}
