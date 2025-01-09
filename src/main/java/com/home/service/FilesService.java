package com.home.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.home.entity.Files;
import com.home.entity.Movies;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @Author: zhazhaming
 * @Date: 2024/10/03/23:03
 */
public interface FilesService extends IService<Files> {

    public boolean uploadPic(MultipartFile file, String userInfo) throws Exception;

}
