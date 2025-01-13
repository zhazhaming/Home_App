package com.home.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.home.entity.Movie_Detail;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * @Author: zhazhaming
 * @Date: 2025/01/11/16:36
 */
@Mapper
public interface MovieDetailMapper extends BaseMapper<Movie_Detail> {

    public List<Movie_Detail> getPopularMovie();

}
