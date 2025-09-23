package com.RecipeCode.teamproject.reci.function.airpollution.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Log4j2
public class AirViewController {

    @GetMapping("/air")
    public String airNow(){
        return "profile/airpanel";
    }
}
