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
    @Transactional
    public boolean uploadPic(MultipartFile file, String useInfo) throws Exception {
        System.out.println ("进入了文件上传service层" );
        // 检查格式
        if (!ALLOWED_FILE_TYPES.contains(file.getContentType())) {
            LogUtils.error ("Invalid file type. Allowed types:" + ALLOWED_FILE_TYPES);
            throw new ServiceException (ResponMsg.FILE_FORMAT_ERROR.status (), ResponMsg.FILE_FORMAT_ERROR.msg ( ));
        }
        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            LogUtils.error ("File size exceeds the maximum allowed size of " + MAX_FILE_SIZE + " bytes.");
            throw new ServiceException(ResponMsg.FLIE_EXCEED_LIMIT.status (), ResponMsg.FLIE_EXCEED_LIMIT.msg ( ));
        }
        // 检查文件名字
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.length() > 255) {
            LogUtils.error ("Invalid file name. File name must be less than 255 characters.");
            throw new ServiceException (ResponMsg.FILE_NAME_EMPTY.status (), ResponMsg.FILE_NAME_EMPTY.msg ( ));
        }
        // 上传文件，并获取图片大小
        String file_path = fileUploadUtils.uploadFile (file);

        // 这里编写获取user_id 将数据写到数据库中
        User user = jsonSerialization.deserializeFromJson (useInfo, User.class);
        int fileSave = this.baseMapper.insert (new Files (UUID.randomUUID ( ).toString ( ), fileName, file.getContentType ( ), file.getSize ( ), new Date ( ), user.getId ( ), file_path, ParameterEnum.PIC_PUBLIC.getParameter ( )));
        if (file_path != "" && fileSave>0) return true;
        return false;
    }
}
