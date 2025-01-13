package com.home.controller;

import com.baomidou.mybatisplus.extension.api.R;
import com.home.Enum.ResponMsg;
import com.home.entity.DTO.MoiveDetailDTO;
import com.home.entity.Movie_Category;
import com.home.entity.Movie_Detail;
import com.home.entity.Movies;
import com.home.service.MovieService;
import com.home.utils.LogUtils;
import io.swagger.annotations.Api;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * @Author: zhazhaming
 * @Date: 2024/05/03/15:52
 */
@RestController
@Api(description = "电影相关接口")
@CrossOrigin
@RequestMapping("movice")

public class MovieController {

    @Autowired
    MovieService movieService;

    @GetMapping("/getMovicesList")
    public R<List<Movies>> getMovices(@RequestParam Integer pageNum,@RequestParam Integer pageSize){
        LogUtils.info ("用户进入电影列表，参数为pageNum:{},pageSize:{}",pageNum,pageSize);
        List<Movies> allMovies = movieService.getAllMovies (pageNum, pageSize);
        return R.ok(allMovies).setCode (ResponMsg.Success.status ());
    }

    @GetMapping("/getByName")
    public R<List<Movies>> getMoviceByName(@RequestParam String name){
        LogUtils.info ("用户进入电影名称查询，参数为name:{}",name);
        List<Movies> moviesByName = movieService.getMoviesByName (name);
        return R.ok(moviesByName).setCode (ResponMsg.Success.status ());
    }

    @GetMapping("/getById")
    public R<Movies> getMoviceById(@RequestParam Integer movie_id) throws Exception {
        LogUtils.info ("用户进入电影ID查询详情，movie_id:{}",movie_id);
        Movies movies = movieService.getMoviesById (movie_id);
        return R.ok (movies).setCode (ResponMsg.Success.status ( ));
    }

    @GetMapping("/detail/getById")
    public R<MoiveDetailDTO>  getMovieDetailById(@RequestParam Integer id) throws Exception {
        LogUtils.info ("Contrller - User enters movie ID to query details，movie_id:{}",id);
        MoiveDetailDTO moiveDetailDTO = movieService.getMovieDetailById (id);
        return R.ok (moiveDetailDTO).setCode (ResponMsg.Success.status ( ));
    }


    @GetMapping("/getMoviceListByTime")
    public R<List<Movies>> getMoviceListByTime(@RequestParam Integer pageNum, @RequestParam Integer pageSize,
                                               @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                               @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate){
        LogUtils.info ("用户进入电影时间查询，参数为pageNum:{},pageSize:{},startDate:{},endDate:{}",pageNum,pageSize,startDate,endDate);
        List<Movies> moviesByTime = movieService.getMoviesByTime (pageNum, pageSize, startDate, endDate);
        return R.ok(moviesByTime).setCode (ResponMsg.Success.status ());
    }

    @GetMapping("/getWellReceive")  // 热映接口
    public R<List<Movies>> getWellReceive(@RequestParam Integer pageNum, @RequestParam Integer pageSize){
        LogUtils.info ("用户进入热映接口，参数为pageNum{},pageSize:{}",pageNum, pageSize);
        List<Movies> movices_list = movieService.getWellReceive (pageNum, pageSize);
        return R.ok (movices_list).setCode (ResponMsg.Success.status ( ));
    }

    @GetMapping("/getHotMovie")
    public R<List<Movies>> getHotMovie(@RequestParam Integer pageNum, @RequestParam Integer pageSize){
        LogUtils.info ("用户进入最受欢迎电影接口，参数为pageNum{},pageSize:{}",pageNum, pageSize);
        List<Movies> movices_list = movieService.getHotMovie (pageNum, pageSize);
        return R.ok (movices_list).setCode (ResponMsg.Success.status ( ));
    }

    @GetMapping("/getPopular")
    public R<List<Movie_Detail>> getPopular(@RequestParam Integer pageNum, @RequestParam Integer pageSize) throws Exception {
        LogUtils.info ("用户进入最受欢迎电影接口，参数为pageNum{},pageSize:{}",pageNum, pageSize);
        List<Movie_Detail> movices_list = movieService.getPopularMovie (pageNum, pageSize);
        return R.ok (movices_list).setCode (ResponMsg.Success.status ( ));
    }

    @GetMapping("/getMovieCategory")
    public R<List<Movie_Category>> getMovieCategory(){
        LogUtils.info ("进入查询电影类别接口");
        List<Movie_Category> movieCategoryList = movieService.getMovieCategory ( );
        return R.ok (movieCategoryList).setCode (ResponMsg.Success.status ( ));
    }

    @GetMapping("/getCategoryMovies")
    public R<List<Movies>> getMovieCategory(@RequestParam Integer categoryId){
        LogUtils.info ("进入查询同类别电影接口");
        List<Movies> movieCategory = movieService.getCategoryMovies (categoryId);
        return R.ok (movieCategory).setCode (ResponMsg.Success.status ());
    }

    @GetMapping("/getMoviesRecent")
    public R<List<Movies>> getMoviesRecent(@RequestParam Integer pageNum, @RequestParam Integer pageSize){
        LogUtils.info ("Access MoviceRecent interface");
        List<Movies> movieRecent = movieService.getMovieRecent (pageNum, pageSize);
        return R.ok (movieRecent).setCode (ResponMsg.Success.status ( ));
    }
}
