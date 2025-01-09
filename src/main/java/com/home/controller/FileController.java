package com.home.controller;

import com.baomidou.mybatisplus.extension.api.R;
import com.home.Enum.ResponMsg;
import com.home.service.FilesService;
import com.home.utils.LogUtils;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @Author: zhazhaming
 * @Date: 2024/10/03/22:56
 */
@RestController
@Api(description = "文件相关接口")
@CrossOrigin
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    FilesService filesService;

    @PostMapping("/uploadPic")
    public R<Boolean> uploadPic(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws Exception {
        LogUtils.info ("进入了文件上传controller层" );
        String userInfo = (String) request.getAttribute ("userInfo");
        System.out.println ("useInfo" + userInfo );
        boolean save = filesService.uploadPic (file, userInfo);
        return R.ok (save).setCode (ResponMsg.Success.status ( ));
    }
}
