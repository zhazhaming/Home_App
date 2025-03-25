package com.home.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.home.Enum.ParameterEnum;
import com.home.Enum.ResponMsg;
import com.home.Exception.ServiceException;

import com.home.entity.DTO.UserLoginDTO;
import com.home.entity.DTO.UserRegistDTO;
import com.home.entity.DTO.UserUpdateDTO;
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

    private static final String REDIS_KEY_SHOT = "user:token:shot";

    private static final String DEFAULT_AVATAR = "http://120.78.1.49/group1/M00/00/00/rBhVEWfVteKAFat3AADG2omeE7U077.jpg";


    @Override
    public UserInfoDTO loginByPassword(UserLoginDTO userLoginDTO) throws Exception {
        LogUtils.info ("User Login ServiceImpl");
        if (! StringUtils.checkValNotNull (userLoginDTO.getNameoremail ()) || ! StringUtils.checkValNotNull (userLoginDTO.getPassword ())){
            throw new ServiceException (ResponMsg.PARAMETER_ERROR.status (), ResponMsg.PARAMETER_ERROR.msg ());
        }
        // 查询当前用户是否存在数据库中
        User user = selectUser (userLoginDTO.getNameoremail ( ), userLoginDTO.getPassword ( ));
        if (user.getUsername () == null || user.getPassword () == null){
            throw new ServiceException (ResponMsg.USER_NOT_EXIST.status (), ResponMsg.USER_NOT_EXIST.msg ());
        }
        //查询redis中，是否包含当前key值
        String user_login_status = REDIS_KEY + user.getId ();
        String user_login_status_shot = REDIS_KEY_SHOT + user.getId ();
        boolean isKeyExist = redisUtils.keyIsExist (user_login_status);
        // 创建返回对象
        UserInfoDTO userInfoDTO = new UserInfoDTO (  );
        // 更新redis中的数据
        if (isKeyExist){ // 存在则更新时间,反序列化返回对象，否则走密码登录的方式设置redis中的token值
            String use_token = redisUtils.get (user_login_status);
            redisUtils.expire (user_login_status, ParameterEnum.USER_LOGIN_EXPIRE_TIME_LONG.getParameter ());
            String user_Serial = jwtUtils.getUserFromToken(use_token);
            return jsonSerialization.deserializeFromJson (user_Serial, UserInfoDTO.class);
        }
        // redis中没有数据，重新存入redis中
        user.setPassword ("");  // 隐藏密码
        BeanUtils.copyProperties (user, userInfoDTO);

        String serialize_user = jsonSerialization.serializeToJson (userInfoDTO);  //将返回对象序列化
        String use_token_long = jwtUtils.generateToken (serialize_user, ParameterEnum.USER_LOGIN_EXPIRE_TIME_LONG.getParameter ());   //序列化后的对象形成长期token
        String use_token_shot = jwtUtils.generateToken (serialize_user, ParameterEnum.USER_LOGIN_EXPIRE_TIME_SHOT.getParameter ());   // 短效token返回前端
        userInfoDTO.setToken (use_token_shot);
        userInfoDTO.setRefreshToken (use_token_long);
        // 保存到redis
        redisUtils.set(user_login_status, use_token_long, ParameterEnum.USER_LOGIN_EXPIRE_TIME_LONG.getParameter ());
        redisUtils.set (user_login_status_shot, use_token_shot, ParameterEnum.USER_LOGIN_EXPIRE_TIME_SHOT.getParameter ());
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
        String touxiang_url = DEFAULT_AVATAR;
        newUser.setAvatar (touxiang_url);
        boolean save = this.save (newUser);
        return save;
    }

    @Override
    public UserInfoDTO getUserInfo(Integer user_id) throws Exception {
        // 校验id和token传递是否正确
        String user_login_status = REDIS_KEY + user_id;
        boolean isKeyExist = redisUtils.keyIsExist (user_login_status);
        if (!isKeyExist){
            throw new ServiceException(ResponMsg.FAIL_USER_LOGIN_EXPIRED.status (), ResponMsg.FAIL_USER_LOGIN_EXPIRED.msg ( ));
        }
        String token = redisUtils.get (user_login_status);
        String user_Serial = jwtUtils.getUserFromToken(token);
        UserInfoDTO userInfoDTO = jsonSerialization.deserializeFromJson (user_Serial, UserInfoDTO.class);
        return userInfoDTO;
    }

    @Override
    public boolean updateUserInfo(UserUpdateDTO userUpdateDTO) {
        System.out.println (userUpdateDTO );
        if(userUpdateDTO == null || userUpdateDTO.getId () == null){
            throw new ServiceException (ResponMsg.Success.status ( ), ResponMsg.FILE_FORMAT_ERROR.msg ( ));
        }
        // 校验用户存在
        User get_user = this.getOne (new LambdaQueryWrapper<User> ( ).eq (User::getId, userUpdateDTO.getId ( )));

        if (get_user == null){
            throw new ServiceException (ResponMsg.Success.status ( ), ResponMsg.FILE_FORMAT_ERROR.msg ( ));
        }
        // 校验旧密码
        String password = get_user.getPassword ();
        String old_password = userUpdateDTO.getOldPassword ( );
        if (! password.equals (old_password)){
            throw new ServiceException (ResponMsg.USER_PASSWORD_FAIL.status ( ), ResponMsg.USER_PASSWORD_FAIL.msg ( ));
        }
        User user = new User (  );
        BeanUtils.copyProperties (userUpdateDTO, user);
        user.setPassword (userUpdateDTO.getNewPassword ());
        boolean save = this.updateById (user);
        return save;
    }

    @Override
    public UserInfoDTO getNewToken(Integer id) throws Exception{
        if (id == null){
            throw new ServiceException (ResponMsg.PARAMETER_ERROR.status (), ResponMsg.PARAMETER_ERROR.msg ());
        }
        String redis_key_target = REDIS_KEY + id;
        boolean exist = redisUtils.keyIsExist (redis_key_target);
        if (exist){
            String user_refresh_token = redisUtils.get (redis_key_target);
            String user_info_serialize = jwtUtils.getUserFromToken (user_refresh_token);
            String user_token_shot = jwtUtils.generateToken (user_info_serialize, ParameterEnum.USER_LOGIN_EXPIRE_TIME_SHOT.getParameter ());
            UserInfoDTO userInfoDTO = jsonSerialization.deserializeFromJson(user_info_serialize, UserInfoDTO.class);
            userInfoDTO.setToken (user_token_shot);
            return userInfoDTO;
        }
        throw new ServiceException (ResponMsg.USER_TOKEN_EXPIRED.status (), ResponMsg.USER_TOKEN_EXPIRED.msg ());
    }

    public User selectUser(String userName, String passWord){
        User user = this.getOne (new LambdaQueryWrapper<User> ( ).eq (User::getUsername, userName).eq (User::getPassword, passWord));
        if (user == null) return new User (  );
        return user;
    }

}
