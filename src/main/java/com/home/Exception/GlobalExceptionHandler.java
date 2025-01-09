package com.home.Exception;

import com.baomidou.mybatisplus.extension.api.R;
import com.home.utils.LogUtils;
import io.swagger.annotations.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

/**
 * @Author: zhazhaming
 * @Date: 2024/07/30/22:23
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<Object> handleServiceException(ServiceException ex, WebRequest request) {
        // 根据需要构建响应体
        R<Object> r_response= new R<> (  ).setCode (ex.getErrorCode ()).setMsg (ex.getErrorMessage ()).setData ("");
        return new ResponseEntity<>( r_response, HttpStatus.valueOf(ex.getErrorCode()));
    }

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<Object> handleAllUncaughtExceptions(Exception ex, WebRequest request) {
//        // 处理所有未捕获的异常
//        LogUtils.error ("An unexpected error occurred: " + ex.getMessage());
//        LogUtils.error ("unexpected error: " + ex);
//
//        R<Object> r_response= new R<> (  ).setCode (500).setMsg ("An unexpected error occurred.").setData ("");
//
//        return new ResponseEntity<>(r_response, HttpStatus.INTERNAL_SERVER_ERROR);
//    }
}
