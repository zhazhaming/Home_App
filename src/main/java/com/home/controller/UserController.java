package com.home.controller;

import com.baomidou.mybatisplus.extension.api.R;
import com.home.Enum.ResponMsg;
import com.home.entity.DTO.UserLoginDTO;
import com.home.entity.DTO.UserInfoDTO;
import com.home.entity.DTO.UserRegistDTO;
import com.home.service.impl.UserServiceImpl;
import com.home.utils.LogUtils;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
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
    public R<UserInfoDTO> login(@RequestBody UserLoginDTO userLoginDTO) throws Exception{
        LogUtils.info ("User Login Controller");
        UserInfoDTO userInfoDTO = userService.loginByPassword (userLoginDTO);
        return R.ok (userInfoDTO).setCode (ResponMsg.Success.status ());
    }

    @Transactional
    @PostMapping("/regist")
    public R<Boolean> regist(@RequestBody UserRegistDTO userRegistDTO){
        LogUtils.info ("User Regist Controller");
        boolean register = userService.register (userRegistDTO);
        System.out.println (register );
        if (register) return R.ok (ResponMsg.USER_REGISTER_SUCCESS.success ()).setCode (ResponMsg.Success.status ( ));
        return R.failed (ResponMsg.USER_REGISTER_FAIL.msg ());
    }
}
