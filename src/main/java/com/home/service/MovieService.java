package com.home.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.home.entity.Movies;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * @Author: zhazhaming
 * @Date: 2024/05/03/16:06
 */
public interface MovieService extends IService<Movies> {

    public List<Movies>  getAllMovies(Integer pageNum, Integer pageSize);

    public List<Movies> getMoviesByName(String name);

    public List<Movies> getMoviesByTime(Integer pageNum, Integer pageSize, LocalDate startDate, LocalDate endDate);
}
