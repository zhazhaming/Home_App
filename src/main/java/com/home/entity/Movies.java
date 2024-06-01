package com.home.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author: zhazhaming
 * @Date: 2024/05/03/16:09
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value="Movie对象", description="")
@TableName("movies")
public class Movies {

    @TableField("ID")
    private int id;

    @TableField("name")
    private String name;

    @TableField("url")
    private String url;

    @TableField("magnet_url")
    private String magnet_url;

    @TableField("img_url")
    private String img_url;

    @TableField("date")
    private Date date;

}
