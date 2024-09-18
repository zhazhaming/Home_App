package com.home.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.home.Enum.PageEnum;
import com.home.Enum.ResponMsg;
import com.home.Exception.ServiceException;
import com.home.config.RedisConfig;
import com.home.entity.Movie_Category;
import com.home.entity.Movies;
import com.home.mapper.MovieCategoryMapper;
import com.home.mapper.MovieMapper;
import com.home.service.MovieService;
import com.home.utils.LogUtils;
import com.home.utils.ParameterValidator;
import com.home.utils.RedisUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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

    @Autowired
    RedisUtils redisUtils;

    @Resource
    private MovieCategoryMapper movieCategoryMapper;

    @Autowired
    private MovieMapper movieMapper;

    private static String MOVIE_REDIS_SEARCH_KEY = "movie:search:";

    /*
        查询所有电影
        return： List<Movies>
     */
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

    /*
        根据名字匹配电影
        return： List<Movies>
     */
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

    /*
        根据ID查询电影
        return： Movies
     */
    @Override
    public Movies getMoviesById(Integer id) {
        LogUtils.info ("Service层-根据电影ID获取电影详情，id:{}",id);
        if (id<0){
            LogUtils.error ("获取电影ID参数小于0");
            throw new ServiceException (ResponMsg.PARAMETER_ERROR.status (), ResponMsg.PARAMETER_ERROR.msg ( ));
        }
        Movies movie = this.getOne (new LambdaQueryWrapper<Movies> ( ).eq (Movies::getId, id));
        // 点击查看过的电影，添加到redis中
        if (movie != null){
            String movie_key = MOVIE_REDIS_SEARCH_KEY+id.toString ();
            if (redisUtils.keyIsExist (movie_key)){
                redisUtils.increment (movie_key, 1);
            }else {
                redisUtils.set (movie_key, 1);
            }
        }
        return movie;
    }

    /*
        根据时间匹配电影
        return： List<Movies>
     */
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

    /*
        查询热映电影
        return： List<Movies>
     */
    @Override
    public List<Movies> getWellReceive(Integer pageNum, Integer pageSize) {
        // 这里编写热映的代码
        List<String> zrevrange = redisUtils.zrevrange (MOVIE_REDIS_SEARCH_KEY, pageNum * pageSize);
        System.out.println (zrevrange );
        return Collections.emptyList ();
    }

    /*
        查询所有电影分类
        return： List<Movie_Category>
     */
    @Override
    public List<Movie_Category> getMovieCategory() {
        List<Movie_Category> movieCategoryList = movieCategoryMapper.selectList (new LambdaQueryWrapper<Movie_Category> ( ));
        if (movieCategoryList == null){
            throw new ServiceException (ResponMsg.SYS_ERROR.status ( ), ResponMsg.SYS_ERROR.msg ( ));
        }
        return movieCategoryList;
    }

    /*
        查询同类电影
        return： List<Movies>
     */
    @Override
    public List<Movies> getCategoryMovies(Integer categoryId) {
        if (categoryId<0){
            throw new ServiceException (ResponMsg.PARAMETER_ERROR.status (), ResponMsg.PARAMETER_ERROR.msg ());
        }
        List<Movies> movieByCategoryList = movieMapper.getMovieByCategory (categoryId);

        return movieByCategoryList;
    }

    /*
        获取最新的电影
        return： List<Movies>
     */
    @Override
    public List<Movies> getMovieRecent(Integer pageNum, Integer pageSize) {
        if (pageNum<1 || pageSize< 1){
            pageNum = PageEnum.RECENT_MOVIE.getPageNumber ();
            pageSize = PageEnum.RECENT_MOVIE.getPageSize ();
        }
        Page<Movies> moviesPage = new Page<> ( pageNum, pageSize );
        QueryWrapper<Movies> wrapperMovie = new QueryWrapper<Movies>();
        wrapperMovie.select ().orderByDesc ("date");
        baseMapper.selectPage (moviesPage, wrapperMovie);
        List<Movies> movieByTimeList = moviesPage.getRecords ( );
        if (movieByTimeList.size ()< 1){
            LogUtils.warn ("Can not search database for Recent Movie, list size is zero");
        }
        return movieByTimeList;
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
