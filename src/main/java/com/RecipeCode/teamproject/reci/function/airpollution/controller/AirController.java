package com.RecipeCode.teamproject.reci.function.airpollution.controller;

import com.RecipeCode.teamproject.reci.auth.dto.SecurityUserDto;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.auth.service.MemberService;
import com.RecipeCode.teamproject.reci.function.airpollution.SidoResolver;
import com.RecipeCode.teamproject.reci.function.airpollution.service.AirService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/air")
@RequiredArgsConstructor
public class AirController {
    private final AirService airService;
    private final MemberService memberService;
    private final SidoResolver sidoResolver;

    @GetMapping("/now")
    public ResponseEntity<JsonNode> now(@RequestParam(required = false) String sido,
                                        @AuthenticationPrincipal SecurityUserDto user) {
        String finalSido = sido;

        if (finalSido == null || finalSido.isBlank()) {
            String email = (user != null ? user.getUsername() : null);
            String userLoc = null;

            if (email != null) {
                Member m = memberService.getByUserEmail(email);
                if (m != null) userLoc = m.getUserLocation();
            }
            finalSido = sidoResolver.resolve(userLoc, "부산");
        } else {
            finalSido = sidoResolver.resolve(finalSido, "부산");
        }

        JsonNode api = airService.getsidoRealtime(finalSido);

        ObjectNode out = JsonNodeFactory.instance.objectNode();
        out.put("sido", finalSido);       // ✅ 서버 최종 시·도
        out.set("response", api);


        return ResponseEntity.ok(out);
    }
}
