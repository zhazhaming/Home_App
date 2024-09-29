package com.home.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.home.entity.Movies;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface MovieMapper extends BaseMapper<Movies> {

    List<Movies> getMovieByCategory(@Param ("categoryId") Integer categoryId);

    List<Movies> getMovieByTime(@Param ("number") Integer number);

    List<Movies> getMovieByIdList(List<Integer> movieId);

    List<Movies> getLastMovies(@Param ("num") Integer num);

}
