package com.home.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.home.entity.Movies;
import com.home.mapper.MovieMapper;
import com.home.service.MovieService;
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
        return MovieList;
        }

    @Override
    public List<Movies> getMoviesByName(String name) {
        if (StringUtils.isBlank (name)) return Collections.emptyList ();
        List<Movies> MovieList = this.list (new LambdaQueryWrapper<Movies> ( ).like (Movies::getName, name));
        Movies movies = this.getOne (new LambdaQueryWrapper<Movies> (  ).eq (Movies::getName, name));
        System.out.println (movies );
        return MovieList;
    }

    public Integer getNumber(){
        int count = this.count (new LambdaQueryWrapper<> ( ));
        return count;
    }
}
