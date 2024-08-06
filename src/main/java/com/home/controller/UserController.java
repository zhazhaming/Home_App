package com.home.controller;

import com.baomidou.mybatisplus.extension.api.R;
import com.home.entity.DTO.UserLoginDTO;
import com.home.entity.VO.UserVo;
import com.home.service.impl.UserServiceImpl;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    UserServiceImpl userService;

    @PostMapping("/login")
    public R<UserVo> login(@RequestBody UserLoginDTO userLoginDTO) throws Exception{
        UserVo userVo = userService.loginByPassword (userLoginDTO);
        return R.ok (userVo);
    }
}
