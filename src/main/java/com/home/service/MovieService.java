package com.home.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.home.entity.Movies;
import io.swagger.models.auth.In;

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

    public Movies getMoviesById(Integer id);

    public List<Movies> getMoviesByTime(Integer pageNum, Integer pageSize, LocalDate startDate, LocalDate endDate);

    public List<Movies> getWellReceive(Integer pageNum, Integer pageSize);
}
