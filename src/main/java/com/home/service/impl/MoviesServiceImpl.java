package com.home.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.home.Enum.PageEnum;
import com.home.Enum.ResponMsg;
import com.home.Exception.ServiceException;
import com.home.entity.Movie_Category;
import com.home.entity.Movies;
import com.home.mapper.MovieCategoryMapper;
import com.home.mapper.MovieMapper;
import com.home.service.MovieService;
import com.home.utils.LogUtils;
import com.home.utils.ParameterValidator;
import com.home.utils.RedisUtils;
import io.swagger.models.auth.In;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    private static String MOVIE_REDIS_ORDER_KEY = "movie:order";

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
            } else {
                redisUtils.set (movie_key, 1);
            }
            if (redisUtils.containsZsetValue (MOVIE_REDIS_ORDER_KEY, id.toString ())){
                redisUtils.incrementZset (MOVIE_REDIS_ORDER_KEY, id.toString (), 1);
            } else{
                redisUtils.szset (MOVIE_REDIS_ORDER_KEY, id.toString (), 1);
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
        if (pageNum*pageSize<=0){
            throw new ServiceException (ResponMsg.PARAMETER_ERROR.status (), ResponMsg.PARAMETER_ERROR.msg ());
        }
        // 获取最新电影
        List<Movies> lastMovieList = getLastMovie (pageNum * pageSize);
        HashMap<Movies, Double> movieMap = new HashMap<> (  );
        for (Movies movies:lastMovieList){
            Integer moviesId = movies.getId ( );
            if (redisUtils.containsZsetValue (MOVIE_REDIS_ORDER_KEY, moviesId.toString ())){
                Double score = redisUtils.getZsetValue (MOVIE_REDIS_ORDER_KEY, moviesId.toString ());
                movieMap.put (movies, score);
            }else {
                movieMap.put (movies, Double.valueOf ("0"));
            }
        }
        // 将map根据key值排序后转化为list
        List<Movies> moviesAllList = movieMap.entrySet ( ).stream ( ).sorted (Map.Entry.comparingByValue (Comparator.reverseOrder ())).map (Map.Entry::getKey).collect (Collectors.toList ( ));
        // 通过steam流自定义分页算法
        List<Movies> moviesWellList = IntStream.range (0, moviesAllList.size ()).filter (i->i>=(pageNum-1)*pageSize && i<pageNum*pageSize).mapToObj (moviesAllList::get).collect(Collectors.toList());
        if (moviesWellList.size ()!= 0){
            return moviesWellList;
        }else {
            LogUtils.warn ("get moviceWellList is zero size");
            return Collections.emptyList ();
        }
    }

    /**
     * 查询最热的电影
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public List<Movies> getPopularMovie(Integer pageNum, Integer pageSize) {
        if (pageNum*pageSize<=0){
            throw new ServiceException (ResponMsg.PARAMETER_ERROR.status (), ResponMsg.PARAMETER_ERROR.msg ());
        }

        // 获取redis zset中的有序列表
        Set<String> stringSet = redisUtils.gzsetOrder (MOVIE_REDIS_ORDER_KEY, 0, pageNum * pageSize);
        List<Integer> MovieIDList = stringSet.stream ( ).map (Integer::parseInt).collect (Collectors.toList ( ));  // 转成Integer类型到数据库中查找
        if (MovieIDList.size () == 0){
            LogUtils.error ("WellMovies interface get Movice ID is Null");
            throw new ServiceException (ResponMsg.SYS_ERROR.status (), ResponMsg.SYS_ERROR.msg ());
        }
        List<Movies> movieDataList = movieMapper.getMovieByIdList (MovieIDList);
        // 将从数据库中获取到的数据按照MovieIDList的顺序进行排序
        List<Movies> movieWellList = changeOrder (MovieIDList, movieDataList);
        return movieWellList;
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

    public List<Movies> changeOrder(List<Integer> number, List<Movies> moviesList){
        Map<Integer, Movies> movieMap = new HashMap<> (  );
        for (Movies movies: moviesList){
            movieMap.put (movies.getId (), movies);
        }
        List<Movies> moviesListResult = new ArrayList<> (  );
        for (Integer id: number){
            Movies movies = movieMap.get (id);
            if (movies != null){
                moviesListResult.add (movies);
            }
        }
        return moviesListResult;
    }

    public List<Movies> getLastMovie(Integer number){
        // 或者最新的电影number条
        List<Movies> lastMoviesList = movieMapper.getLastMovies (number);
        return lastMoviesList;
    }


}
