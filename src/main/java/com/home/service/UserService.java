package com.home.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.home.entity.DTO.UserLoginDTO;
import com.home.entity.DTO.UserRegistDTO;
import com.home.entity.DTO.UserUpdateDTO;
import com.home.entity.User;
import com.home.entity.DTO.UserInfoDTO;

/**
 * @Author: zhazhaming
 * @Date: 2024/07/28/18:29
 */
public interface UserService extends IService<User> {

    public UserInfoDTO loginByPassword(UserLoginDTO userLoginDTO) throws Exception;

    public boolean register(UserRegistDTO userRegistDTO);

    public UserInfoDTO getUserInfo(Integer id) throws Exception;

    public boolean updateUserInfo(UserUpdateDTO userUpdateDTO);

    public UserInfoDTO getNewToken(Integer id) throws Exception;

}
