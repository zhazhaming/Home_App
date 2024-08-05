package com.home.entity.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: zhazhaming
 * @Date: 2024/07/30/22:08
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginDTO {

    public String nameoremail;

    public String password;
}
