package com.home.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.additional.query.impl.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.home.Enum.ResponMsg;
import com.home.Exception.ServiceException;
import com.home.config.RedisConfig;
import com.home.entity.DTO.UserLoginDTO;
import com.home.entity.User;
import com.home.entity.VO.UserVo;
import com.home.mapper.UserMapper;
import com.home.service.UserService;

import com.home.utils.JsonSerialization;
import com.home.utils.RedisUtils;
import com.home.utils.RestTemplateUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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

    private static final String REDIS_KEY = "user:login:";

    @Override
    public UserVo loginByPassword(UserLoginDTO userLoginDTO) throws Exception {
        if (StringUtils.checkValNotNull (userLoginDTO.getNameoremail ()) || StringUtils.checkValNotNull (userLoginDTO.getPassword ())){
            throw new ServiceException (ResponMsg.PARAMETER_ERROR.status (), ResponMsg.PARAMETER_ERROR.msg ());
        }
        // 查询当前用户是否存在数据库中
        User user = selectUser (userLoginDTO.getNameoremail ( ), userLoginDTO.getPassword ( ));
        if (user == null){
            throw new ServiceException (ResponMsg.USER_NOT_EXIST.status (), ResponMsg.USER_NOT_EXIST.msg ());
        }
        //查询redis中，是否包含当前key值
        String user_login_status = REDIS_KEY + user.getId ();
        boolean isKeyExist = redisUtils.keyIsExist (user_login_status);
        if (isKeyExist){ // 存在则更新时间，否则走登录的方式
            redisUtils.expire (user_login_status, 60 * 60*12);
        }
        else {
            user.setPassword ("");
            redisUtils.set(user_login_status, jsonSerialization.serializeToJson (user), 60 * 60*12);
        }
        UserVo userVo = new UserVo (  );
        BeanUtils.copyProperties (user, userVo);
        return userVo;
    }

    @Override
    public boolean register(UserLoginDTO userLoginDTO) {
        return false;
    }


    public User selectUser(String userName, String passWord){
        User user = this.getOne (new LambdaQueryWrapper<User> ( ).eq (User::getNickname, userName).eq (User::getPassword, passWord));
        if (user == null) return new User (  );
        return user;
    }
}
