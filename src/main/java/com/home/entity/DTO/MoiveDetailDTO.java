package com.home.entity.DTO;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @Author: zhazhaming
 * @Date: 2025/01/11/17:30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value="Movie详情对象", description="")
public class MoiveDetailDTO {

    @NotNull
    private int movie_id;

    private String title;

    private int year;

    private String origin;

    private String category;

    private String language;

    private Date release_date;

    private float douban_rating;

    private int duration;

    private String director;

    private String screenwriter;

    private String actors;

    private String description;

    private String name;

    private String url;

    private String magnet_url;

    private String img_url;

    private Date date;

}