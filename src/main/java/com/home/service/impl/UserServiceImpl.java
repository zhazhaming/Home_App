package com.home.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.home.Enum.ParameterEnum;
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

    private static final String REDIS_KEY = "user:token:";


    @Override
    public UserInfoDTO loginByPassword(UserLoginDTO userLoginDTO) throws Exception {
        LogUtils.info ("User Login ServiceImpl");
        if (! StringUtils.checkValNotNull (userLoginDTO.getNameoremail ()) || ! StringUtils.checkValNotNull (userLoginDTO.getPassword ())){
            throw new ServiceException (ResponMsg.PARAMETER_ERROR.status (), ResponMsg.PARAMETER_ERROR.msg ());
        }
        // 查询当前用户是否存在数据库中
        User user = selectUser (userLoginDTO.getNameoremail ( ), userLoginDTO.getPassword ( ));
        System.out.println (user );
        if (user.getUsername () == null || user.getPassword () == null){
            throw new ServiceException (ResponMsg.USER_NOT_EXIST.status (), ResponMsg.USER_NOT_EXIST.msg ());
        }
        //查询redis中，是否包含当前key值
        String user_login_status = REDIS_KEY + user.getId ();
        boolean isKeyExist = redisUtils.keyIsExist (user_login_status);
        // 创建返回对象
        UserInfoDTO userInfoDTO = new UserInfoDTO (  );
        // 更新redis中的数据
        if (isKeyExist){ // 存在则更新时间,反序列化返回对象，否则走密码登录的方式设置redis中的token值
            String use_token = redisUtils.get (user_login_status);
            redisUtils.expire (user_login_status, ParameterEnum.USER_LOGIN_EXPIRE_TIME.getParameter ());
            String user_Serial = jwtUtils.getUserFromToken(use_token);
            return jsonSerialization.deserializeFromJson (user_Serial, UserInfoDTO.class);
        }
        else {
            user.setPassword ("");  // 隐藏密码
            BeanUtils.copyProperties (user, userInfoDTO);

            String serialize_user = jsonSerialization.serializeToJson (userInfoDTO);  //将返回对象序列化
            String use_token = jwtUtils.generateToken (serialize_user, ParameterEnum.USER_LOGIN_EXPIRE_TIME.getParameter ());   //序列化后的对象形成token
            userInfoDTO.setToken (use_token);
            System.out.println ("token:" + use_token);
            redisUtils.set(user_login_status, use_token, ParameterEnum.USER_LOGIN_EXPIRE_TIME.getParameter ());
        }

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
        String touxiang_url = "http://120.78.1.49/group1/M00/00/00/rBhVEWfVteKAFat3AADG2omeE7U077.jpg";
        newUser.setAvatar (touxiang_url);
        boolean save = this.save (newUser);
        return save;
    }

    @Override
    public UserInfoDTO getUserInfo(Integer user_id, String authHeader) throws Exception {
        // 校验id和token传递是否正确
        if (user_id == null || authHeader == null || !authHeader.startsWith("Bearer ")){
            throw new ServiceException(ResponMsg.PARAMETER_ERROR.status (), ResponMsg.PARAMETER_ERROR.msg ( ));
        }
        String user_login_status = REDIS_KEY + user_id;
        boolean isKeyExist = redisUtils.keyIsExist (user_login_status);
        if (!isKeyExist){
            throw new ServiceException(ResponMsg.FAIL_USER_LOGIN_EXPIRED.status (), ResponMsg.FAIL_USER_LOGIN_EXPIRED.msg ( ));
        }
        String token = authHeader.split(" ")[1];
        System.out.println ("前端获取token："+token);
        String user_Serial = jwtUtils.getUserFromToken(token);
        UserInfoDTO userInfoDTO = jsonSerialization.deserializeFromJson (user_Serial, UserInfoDTO.class);
        return userInfoDTO;
    }

    public User selectUser(String userName, String passWord){
        User user = this.getOne (new LambdaQueryWrapper<User> ( ).eq (User::getUsername, userName).eq (User::getPassword, passWord));
        if (user == null) return new User (  );
        return user;
    }

}
