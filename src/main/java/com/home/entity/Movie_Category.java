package com.home.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: zhazhaming
 * @Date: 2024/09/14/23:20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value="Movie分类对象", description="")
@TableName("categories")
public class Movie_Category {

    @TableField("category_id")
    Integer category_id;

    @TableField("category_name")
    String category_name;
}
