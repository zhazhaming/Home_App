package com.home.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.home.Enum.ParameterEnum;
import com.home.Enum.ResponMsg;
import com.home.Exception.ServiceException;
import com.home.entity.Files;
import com.home.entity.User;
import com.home.mapper.FilesMapper;
import com.home.service.FilesService;
import com.home.utils.FileUploadUtils;
import com.home.utils.JsonSerialization;
import com.home.utils.LogUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @Author: zhazhaming
 * @Date: 2024/10/03/23:05
 */
@Service
public class FilesServiceImpl extends ServiceImpl<FilesMapper, Files> implements FilesService {

    @Autowired
    public FileUploadUtils fileUploadUtils;

    @Autowired
    public JsonSerialization jsonSerialization;

    private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/gif", "video/mp4","video/avi", "video/mov");

    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB

    @Override
    public String uploadPic(MultipartFile file) throws Exception {
        LogUtils.info("进入了文件上传service层，文件名: " + file.getOriginalFilename());
        
        try {
            // 检查格式
            if (!ALLOWED_FILE_TYPES.contains(file.getContentType())) {
                LogUtils.error("Invalid file type. Allowed types:" + ALLOWED_FILE_TYPES);
                throw new ServiceException(ResponMsg.FILE_FORMAT_ERROR.status(), ResponMsg.FILE_FORMAT_ERROR.msg());
            }
            
            // 检查文件大小
            if (file.getSize() > MAX_FILE_SIZE) {
                LogUtils.error("File size exceeds the maximum allowed size of " + MAX_FILE_SIZE + " bytes.");
                throw new ServiceException(ResponMsg.FLIE_EXCEED_LIMIT.status(), ResponMsg.FLIE_EXCEED_LIMIT.msg());
            }
            
            // 检查文件名字
            String fileName = file.getOriginalFilename();
            if (fileName == null || fileName.length() > 255) {
                LogUtils.error("Invalid file name. File name must be less than 255 characters.");
                throw new ServiceException(ResponMsg.FILE_NAME_EMPTY.status(), ResponMsg.FILE_NAME_EMPTY.msg());
            }
            
            // 上传文件，并获取图片大小
            String file_path = fileUploadUtils.uploadFile(file);
            
            if (file_path != null && !file_path.isEmpty()) {
                LogUtils.info("文件上传成功，路径: " + file_path);
                return file_path;
            } else {
                LogUtils.error("文件上传失败，返回路径为空");
                throw new ServiceException(ResponMsg.FILE_UPLOAD_FAILED.status(), "文件上传失败");
            }
            
        } catch (ServiceException e) {
            // 重新抛出业务异常
            throw e;
        } catch (Exception e) {
            LogUtils.error("文件上传过程中发生未知异常: " + e.getMessage(), e);
            throw new ServiceException(ResponMsg.FILE_UPLOAD_FAILED.status(), "文件上传失败: " + e.getMessage());
        }
    }
}
