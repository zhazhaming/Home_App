package com.home.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @Author: zhazhaming
 * @Date: 2026/10/11
 * @Description: 根路径跳转到Swagger接口文档页，便于浏览器直接访问验证服务状态
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "redirect:/swagger-ui.html";
    }
}
