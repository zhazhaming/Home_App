package com.home.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.home.entity.Movies;
import com.home.mapper.MovieMapper;
import com.home.service.MovieService;
import com.home.utils.LogUtils;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * @Author: zhazhaming
 * @Date: 2024/05/03/16:07
 */
@Service
public class MoviesServiceImpl extends ServiceImpl<MovieMapper, Movies>  implements MovieService {

    @Override
    public List<Movies> getAllMovies(Integer pageNum, Integer pageSize) {
        LogUtils.info ("Service层-获取电影列表，pageNum:{},pageSize:{}",pageNum,pageSize);
        Integer movieNumber = getNumber ( );
        if (pageSize<0){
            pageSize = 10;
        }
        if (pageSize>movieNumber){
            pageSize = movieNumber;
        }
        if (movieNumber/pageSize<pageNum){
            pageNum = movieNumber/pageSize;
        }
        Page<Movies> moviesPage = new Page<> (pageNum,pageSize);
        QueryWrapper<Movies> wrapperMovie = new QueryWrapper<Movies>();
        wrapperMovie.select ().orderByDesc ("date");
        baseMapper.selectPage (moviesPage, wrapperMovie);
        List<Movies> MovieList = moviesPage.getRecords ( );
        LogUtils.info ("Service层-获取电影列表数据量{}，数据列表:{}",MovieList.size (),MovieList);
        return MovieList;
        }

    @Override
    public List<Movies> getMoviesByName(String name) {
        LogUtils.info ("Service层-根据电影名获取电影名称，name:{}",name);
        if (StringUtils.isBlank (name)) {
            LogUtils.warn ("电影名称为空,返回了空列表");
            return Collections.emptyList ();
        }
        List<Movies> MovieList = this.list (new LambdaQueryWrapper<Movies> ( ).like (Movies::getName, name));
        LogUtils.info ("Service层-根据电影名获取电影数据:{}",MovieList);
        return MovieList;
    }

    public Integer getNumber(){
        int count = this.count (new LambdaQueryWrapper<> ( ));
        LogUtils.info ("获取到数据库中的电影数量为:{}",count);
        return count;
    }
}
