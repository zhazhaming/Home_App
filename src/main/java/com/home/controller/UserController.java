package com.home.controller;

import com.home.entity.DTO.UserLoginDTO;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: zhazhaming
 * @Date: 2024/07/28/17:09
 */
@RestController
@Api(description = "用户相关接口")
@CrossOrigin
@RequestMapping("user")
public class UserController {

    @RequestMapping("/login")
    public String login(@RequestBody UserLoginDTO userLoginDTO){

        return "login success";
    }
}
