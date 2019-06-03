package com.bhfantasy.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ViewController {
    @RequestMapping({
            "/maintenance", "/page-not-found",
            "/leagues", "/leagues/**",
            "/teams", "/teams/**",
            "/admin",
            "/players", "/players/**" })
    public String index() {
        return "forward:/index.html";
    }
}