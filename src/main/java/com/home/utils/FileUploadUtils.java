package com.home.utils;

import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @Author: zhazhaming
 * @Date: 2024/10/03/23:09
 */
@Component
public class FileUploadUtils {

    @Autowired
    private FastFileStorageClient storageClient;

    public String uploadFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }
        try{
            StorePath storePath = storageClient.uploadFile(file.getInputStream(), file.getSize(),
                    getFileExtension(file.getOriginalFilename()), null);
            return storePath.getFullPath();
        }catch (IOException e){
            LogUtils.error("Failed to upload file: " + e.getMessage(), e);
            throw e; // 或者你可以选择抛出更具体的异常
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            LogUtils.error ("upload fileName is Empty, check please!!");
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
