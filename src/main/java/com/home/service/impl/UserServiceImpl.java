package com.home.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.home.Enum.ResponMsg;
import com.home.Exception.ServiceException;

import com.home.entity.DTO.UserLoginDTO;
import com.home.entity.DTO.UserRegistDTO;
import com.home.entity.User;
import com.home.entity.DTO.UserInfoDTO;
import com.home.mapper.UserMapper;
import com.home.service.UserService;

import com.home.utils.JWTUtils;
import com.home.utils.JsonSerialization;
import com.home.utils.LogUtils;
import com.home.utils.RedisUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * @Author: zhazhaming
 * @Date: 2024/07/30/22:01
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    public RedisUtils redisUtils;

    @Autowired
    public JsonSerialization jsonSerialization;

    @Autowired
    public JWTUtils jwtUtils;

    private static final String REDIS_KEY = "user:login:";

    private static final Integer USER_LOGIN_EXPIRE_TIME = 60 * 60 * 12;  //用户token过期时间

    @Override
    public UserInfoDTO loginByPassword(UserLoginDTO userLoginDTO) throws Exception {
        LogUtils.info ("User Login ServiceImpl");
        if (! StringUtils.checkValNotNull (userLoginDTO.getNameoremail ()) || ! StringUtils.checkValNotNull (userLoginDTO.getPassword ())){
            throw new ServiceException (ResponMsg.PARAMETER_ERROR.status (), ResponMsg.PARAMETER_ERROR.msg ());
        }
        // 查询当前用户是否存在数据库中
        User user = selectUser (userLoginDTO.getNameoremail ( ), userLoginDTO.getPassword ( ));
        if (user == null){
            throw new ServiceException (ResponMsg.USER_NOT_EXIST.status (), ResponMsg.USER_NOT_EXIST.msg ());
        }
        //查询redis中，是否包含当前key值
        String user_login_status = REDIS_KEY + user.getId ();
        String serialize_user = jsonSerialization.serializeToJson (user);
        boolean isKeyExist = redisUtils.keyIsExist (user_login_status);
        if (isKeyExist){ // 存在则更新时间，否则走登录的方式
            redisUtils.expire (user_login_status, USER_LOGIN_EXPIRE_TIME);
        }
        else {
            user.setPassword ("");
            redisUtils.set(user_login_status, jsonSerialization.serializeToJson (user), 60 * 60*12);
        }
        //创建token
        UserInfoDTO userInfoDTO = new UserInfoDTO (  );
        BeanUtils.copyProperties (user, userInfoDTO);
        String token = jwtUtils.generateToken (serialize_user, 60*60*12);
        userInfoDTO.setToken (token);
        return userInfoDTO;
    }

    @Override
    public boolean register(UserRegistDTO userRegistDTO) {
        // 查询数据是否包含这个用户
        LogUtils.info ("User Regist ServiceImpl");
        User user = this.getOne (new LambdaQueryWrapper<User> ( ).eq (User::getEmail, userRegistDTO.getEmail ()));
        if (user != null){
            throw new ServiceException(ResponMsg.USER_IS_EXIST.status (), ResponMsg.USER_IS_EXIST.msg ());
        }
        // 创建用户
        User newUser = new User (  );
        BeanUtils.copyProperties (userRegistDTO, newUser);
        boolean save = this.save (newUser);
        return save;
    }


    public User selectUser(String userName, String passWord){
        User user = this.getOne (new LambdaQueryWrapper<User> ( ).eq (User::getUsername, userName).eq (User::getPassword, passWord));
        if (user == null) return new User (  );
        return user;
    }
}
