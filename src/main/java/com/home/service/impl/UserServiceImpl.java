package com.home.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.home.Enum.ResponMsg;
import com.home.Exception.ServiceException;
import com.home.entity.DTO.UserLoginDTO;
import com.home.entity.User;
import com.home.entity.VO.UserVo;
import com.home.mapper.UserMapper;
import com.home.service.UserService;

import org.springframework.stereotype.Service;


/**
 * @Author: zhazhaming
 * @Date: 2024/07/30/22:01
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public UserVo loginByPassword(UserLoginDTO userLoginDTO) {
        if (StringUtils.checkValNotNull (userLoginDTO.getNameoremail ()) || StringUtils.checkValNotNull (userLoginDTO.getPassword ())){
            throw new ServiceException (ResponMsg.PARAMETER_ERROR.status (), ResponMsg.PARAMETER_ERROR.msg ());
        }
        //查询redis中，是否还包含这样的图片

        return null;
    }

    @Override
    public boolean register(UserLoginDTO userLoginDTO) {
        return false;
    }
}
