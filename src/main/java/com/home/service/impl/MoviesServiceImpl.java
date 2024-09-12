package com.home.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.home.Enum.PageEnum;
import com.home.Enum.ResponMsg;
import com.home.Exception.ServiceException;
import com.home.entity.Movies;
import com.home.mapper.MovieMapper;
import com.home.service.MovieService;
import com.home.utils.LogUtils;
import com.home.utils.ParameterValidator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerErrorException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @Author: zhazhaming
 * @Date: 2024/05/03/16:07
 */
@Service
public class MoviesServiceImpl extends ServiceImpl<MovieMapper, Movies>  implements MovieService {

    @Autowired
    ParameterValidator parameterValidator;

    @Override
    public List<Movies> getAllMovies(Integer pageNum, Integer pageSize) {
        LogUtils.info ("Service层-获取电影列表，pageNum:{},pageSize:{}",pageNum,pageSize);
        Integer movieNumber = getTotalNumber ( );
        Integer movicePageSize = getPageSize (pageSize, movieNumber);
        Integer pageNumber = getPageNumber (movicePageSize, pageNum, movieNumber);
        Page<Movies> moviesPage = new Page<> (pageNumber,movicePageSize);
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

    @Override
    public Movies getMoviesById(Integer id) {
        LogUtils.info ("Service层-根据电影ID获取电影详情，id:{}",id);
        if (id<0){
            LogUtils.error ("获取电影ID参数小于0");
            throw new ServiceException (ResponMsg.PARAMETER_ERROR.status (), ResponMsg.PARAMETER_ERROR.msg ( ));
        }
        Movies movie = this.getOne (new LambdaQueryWrapper<Movies> ( ).eq (Movies::getId, id));
        return movie;
    }

    @Override
    public List<Movies> getMoviesByTime(Integer pageNum, Integer pageSize, LocalDate startDate, LocalDate endDate) {
        LogUtils.info ("Service层-根据时间获取电影列表，pageNum:{},pageSize:{},startDate:{},endDate:{}",pageNum,pageSize,startDate,endDate);
        Integer movieNumber = getTotalNumber ( );
        Integer movicePageSize = getPageSize (pageSize, movieNumber);
        Integer pageNumber = getPageNumber (movicePageSize, pageNum, movieNumber);
        Page<Movies> moviesPage = new Page<> (pageNumber, movicePageSize);
        QueryWrapper<Movies> wrapperMovie = new QueryWrapper<Movies>();
        wrapperMovie.select ().between ("date",startDate,endDate).orderByDesc ("date");
        baseMapper.selectPage (moviesPage, wrapperMovie);
        List<Movies> moviesList = moviesPage.getRecords ( );
        LogUtils.info ("Service层-根据时间获取电影列表数量:{},数据列表:{}",moviesList.size (),moviesList);
        return moviesList;
    }

    @Override
    public List<Movies> getWellReceive(Integer pageNum, Integer pageSize) {
        // 这里编写热饮的代码，hhh
        return null;
    }

    public Integer getTotalNumber(){
        int count = this.count (new LambdaQueryWrapper<> ( ));
        LogUtils.info ("获取到数据库中的电影数量为:{}",count);
        return count;
    }

    public Integer getPageSize(Integer pageSize, Integer pageTotal){
        if (pageSize<0){
            pageSize = PageEnum.PAGE_MOVICE.getPageSize ();
        }
        if (pageSize>pageTotal){
            pageSize = pageTotal;
        }
        return pageSize;
    }

    public Integer getPageNumber(Integer pageSize, Integer pageNum, Integer pageTotal){
        if (pageNum<0){
            pageNum = PageEnum.PAGE_MOVICE.getPageNumber ();
        }
        if (pageTotal/pageSize<pageNum){
            pageNum = pageTotal/pageSize;
        }
        return pageNum;
    }


}
