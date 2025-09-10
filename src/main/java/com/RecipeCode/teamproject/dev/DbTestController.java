package com.RecipeCode.teamproject.dev;


import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DbTestController {
    private final JdbcTemplate jdbc;

    public DbTestController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping("/dev/db-ping")
    public Map<String, Object> ping() {
        Integer one = jdbc.queryForObject("select 1 from dual", Integer.class);
        String now = jdbc.queryForObject(
                "select to_char(sysdate,'YYYY-MM-DD HH24:MI:SS') from dual", String.class);
        return Map.of("ok", one != null && one == 1, "sysdate", now);
    }

}
