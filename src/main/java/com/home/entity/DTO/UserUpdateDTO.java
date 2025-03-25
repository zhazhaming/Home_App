package com.home.entity.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: zhazhaming
 * @Date: 2025/03/23/0:22
 * 用户提交修改密码的数据对象传输
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDTO {

    private Integer id;

    private String username;

    private String oldPassword;

    private String newPassword;

    private String phone;

    private Integer gender;

    private String avatar;
}
