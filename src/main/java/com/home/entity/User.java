package com.home.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @Author: zhazhaming
 * @Date: 2024/07/28/16:15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value="User对象", description="")
@TableName("users")
public class User {

    @TableField("ID")
    @NotNull
    private int id;

    @TableField("nickname")
    @NotNull
    private String nickname;

    @TableField("password")
    @NotNull
    private String password;

    @TableField("email")
    @NotNull
    private String email;

    @TableField("phone")
    private String phone;

    @TableField("gender")
    private int gender;   // 0代表男生，1代表女生

    @TableField("avatar")
    private String avatar;
}
