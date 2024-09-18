package com.home.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: zhazhaming
 * @Date: 2024/09/14/23:09
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value="Movie分类映射对象", description="")
@TableName("movie_category")
public class Movies_Category_Cssociations {

    @TableField("id")
    Integer id;

    @TableField("movie_id")
    Integer movie_id;

    @TableField("category_id")
    Integer category_id;
}
