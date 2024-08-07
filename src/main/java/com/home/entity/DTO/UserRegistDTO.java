package com.home.entity.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: zhazhaming
 * @Date: 2024/08/07/22:18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
//接收前端的请求
public class UserRegistDTO {

    private String username;
    private String password;
    private String email;

}
