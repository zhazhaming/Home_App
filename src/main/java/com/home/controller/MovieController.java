package com.home.controller;

import com.baomidou.mybatisplus.extension.api.R;
import com.home.Enum.ResponMsg;
import com.home.entity.Movies;
import com.home.service.MovieService;
import com.home.utils.LogUtils;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
}
