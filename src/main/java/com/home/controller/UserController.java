package com.home.controller;

import com.baomidou.mybatisplus.extension.api.R;
import com.home.Enum.ResponMsg;
import com.home.entity.DTO.UserLoginDTO;
import com.home.entity.DTO.UserInfoDTO;
import com.home.entity.DTO.UserRegistDTO;
import com.home.entity.DTO.UserUpdateDTO;
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

    @RequestMapping("/info")
    public R<UserInfoDTO> GetUserInfo(@RequestParam Integer id) throws Exception{
        LogUtils.info ("Get User Info Controller");
        UserInfoDTO userInfo = userService.getUserInfo (id);
        if (userInfo.getId () != null){
            return R.ok (userInfo).setCode (ResponMsg.Success.status ( ));
        }
        return R.failed (ResponMsg.SYS_ERROR.msg ());
    }

    @PostMapping("/update")
    public R<Boolean> updateUserInfo(@RequestBody UserUpdateDTO userUpdateDTO){
        LogUtils.info ("Update User Info Controller");
        boolean is_save = userService.updateUserInfo (userUpdateDTO);
        return R.ok (is_save).setCode (ResponMsg.Success.status ( ));
    }

    @PostMapping("refresh")  //更新长效token
    public R<UserInfoDTO> GetNewToken(@RequestParam Integer id) throws Exception {
        LogUtils.info("Get User New Token Controller，用户ID: " + id);
        try {
            UserInfoDTO userInfoDTO = userService.getNewToken(id);
            LogUtils.info("成功生成新token，用户ID: " + id);
            return R.ok(userInfoDTO).setCode(ResponMsg.Success.status());
        } catch (Exception e) {
            LogUtils.error("获取新token失败，用户ID: " + id + ", 错误: " + e.getMessage(), e);
            return R.failed ("获取新token失败，请重新登录");
        }
    }

    @PostMapping("/logout")  //退出登录
    public R<Boolean> logout(@RequestParam Integer id) {
        LogUtils.info("User Logout Controller，用户ID: " + id);
        try {
            boolean result = userService.logout(id);
            if (result) {
                LogUtils.info("用户退出登录成功，用户ID: " + id);
                return R.ok(true).setCode(ResponMsg.Success.status());
            } else {
                LogUtils.warn("用户退出登录失败，用户ID: " + id);
                return R.failed("退出登录失败");
            }
        } catch (Exception e) {
            LogUtils.error("退出登录时发生异常，用户ID: " + id + ", 错误: " + e.getMessage(), e);
            return R.failed("退出登录失败: " + e.getMessage());
        }
    }
}
