package com.home.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @Author: zhazhaming
 * @Date: 2025/01/11/15:48
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value="Movie详情对象", description="")
@TableName("movie_index")
public class Movie_Detail {

    @TableField("ID")
    private int id;

    @TableField("movie_id")
    @NotNull
    private int movie_id;

    @TableField("title")
    private String title;

    @TableField("year")
    private int year;

    @TableField("origin")
    private String origin;

    @TableField("category")
    private String category;

    @TableField("language")
    private String language;

    @TableField("release_date")
    private Date release_date;

    @TableField("douban_rating")
    private float douban_rating;

    @TableField("duration")
    private int duration;

    @TableField("director")
    private String director;

    @TableField("screenwriter")
    private String screenwriter;

    @TableField("actors")
    private String actors;

    @TableField("description")
    private String description;

}
