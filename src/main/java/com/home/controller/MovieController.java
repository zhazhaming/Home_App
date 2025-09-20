package com.home.controller;

import com.baomidou.mybatisplus.extension.api.R;
import com.home.Enum.ResponMsg;
import com.home.entity.DTO.MoiveDetailDTO;
import com.home.entity.Movie_Category;
import com.home.entity.Movies;
import com.home.entity.Reservation;
import com.home.service.MovieService;
import com.home.service.ReservationService;
import com.home.entity.DTO.ReservationRequestDTO;
import com.home.entity.DTO.CancelReservationRequestDTO;
import com.home.utils.LogUtils;
import com.home.utils.QueueMonitor;
import com.home.utils.ResponseCache;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    ReservationService reservationService;

    @Autowired
    private QueueMonitor queueMonitor;

    @Autowired
    private ResponseCache responseCache;

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
    public R<List<Movies>> getPopular(@RequestParam Integer pageNum, @RequestParam Integer pageSize) throws Exception {
        LogUtils.info ("用户进入最受欢迎电影接口，参数为pageNum{},pageSize:{}",pageNum, pageSize);
        List<Movies> movices_list = movieService.getPopularMovie (pageNum, pageSize);
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

    @PostMapping("/reservation")
    public R<String> reserveMovie(@RequestBody ReservationRequestDTO request) throws Exception {
        LogUtils.info("用户提交电影预约: title:{}, email:{}, userId:{}, username:{}", request.getTitle(), request.getNotifyEmail(), request.getUserId(), request.getUsername());
        boolean queued = reservationService.sendReservationMessage(request);
        if (!queued) {
            return R.ok("电影已存在or电影已经预约过，无需预约").setCode(ResponMsg.Success.status());
        }
        return R.ok("预约已提交").setCode(ResponMsg.Success.status());
    }

    @PostMapping("/cancelReservation")
    public R<String> cancelReservation(@RequestBody CancelReservationRequestDTO request) throws Exception {
        LogUtils.info("用户取消电影预约: title:{}, userId:{}, username:{}", request.getTitle(), request.getUserId(), request.getUsername());
        boolean cancelled = reservationService.sendCancelReservationMessage(request);
        if (!cancelled) {
            return R.ok("预约不存在或已取消，无需操作").setCode(ResponMsg.Success.status());
        }
        return R.ok("取消预约已提交").setCode(ResponMsg.Success.status());
    }

    @GetMapping("/getReservation")
    public R<List<Reservation>> getReservation(@RequestParam Integer userId, @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer pageSize) {
        LogUtils.info("用户查询电影预约: userId:{}, page:{}, pageSize:{}", userId, page, pageSize);
        
        try {
            // 设置默认值
            if (page == null || page <= 0) {
                page = 1;
            }
            if (pageSize == null || pageSize <= 0) {
                pageSize = 20;
            }
            
            // 调用Service层处理业务逻辑
            List<Reservation> reservations = reservationService.getReservationPageById(userId, page, pageSize);
            return R.ok(reservations).setCode(ResponMsg.Success.status());
            
        } catch (Exception e) {
            LogUtils.error("查询用户预约记录失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            return R.failed("查询预约记录失败: " + e.getMessage());
        }
    }

    @GetMapping("/getReservationSize")
    public R<Integer> getReservation(@RequestParam Integer userId) {
        LogUtils.info("用户查询电影预约数量查询: userId:{}", userId);
        try {
            Integer reservationSize = reservationService.getTotalReservation (userId);
            return R.ok(reservationSize).setCode(ResponMsg.Success.status());
        } catch (Exception e) {
            LogUtils.error("用户查询电影预约数量查询，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            return R.failed("查询预约数量失败: " + e.getMessage());
        }
    }


    // 模拟“新电影上线”触发，实际可在新增电影后调用
    @PostMapping("/publishNewMovieOnline")
    public R<String> publishNewMovieOnline(@RequestParam String title) throws Exception {
        LogUtils.info("新电影上线触发，标题:{}，开始处理与之匹配的预约", title);
        int processed = reservationService.consumeReservationForTitle(title);
        return R.ok("已处理预约条数:" + processed).setCode(ResponMsg.Success.status());
    }

    @GetMapping("/reservation/status")
    public R<java.util.Map<String, Object>> getReservationStatus() {
        try {
            // 获取队列状态
            QueueMonitor.QueueStats queueStats = queueMonitor.getQueueStats();
            
            // 获取缓存状态
            ResponseCache.CacheStats cacheStats = responseCache.getStats();
            
            // 构建状态信息
            Map<String, Object> status = new HashMap<> ();
            status.put("queueStats", queueStats);
            status.put("cacheStats", cacheStats);
            status.put("timestamp", System.currentTimeMillis());
            
            return R.ok (status).setCode(ResponMsg.Success.status());
        } catch (Exception ex) {
            LogUtils.error("获取预约状态失败: {}", ex.getMessage());
            Map<String, Object> errorStatus = new HashMap<>();
            errorStatus.put("error", "获取状态失败");
            errorStatus.put("message", ex.getMessage());
            errorStatus.put("timestamp", System.currentTimeMillis());
            return R.ok(errorStatus).setCode(ResponMsg.Error.status());
        }
    }
    
}

