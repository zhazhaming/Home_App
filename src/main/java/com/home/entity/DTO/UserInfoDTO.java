package com.home.entity.DTO;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @Author: zhazhaming
 * @Date: 2024/08/04/17:27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
// 返回给前端的数据
public class UserInfoDTO {

    @NotNull
    private String nickname;

    @NotNull
    private String email;

    private String phone;

    private int gender;   // 0代表男生，1代表女生

    private String avatar;

    private String token;
}
