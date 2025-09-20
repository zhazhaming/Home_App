package com.home.utils;

import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

/**
 * @Author: zhazhaming
 * @Date: 2024/10/03/23:09
 */
@Component
public class FileUploadUtils {

    @Autowired
    private FastFileStorageClient storageClient;

    @Value("${fdfs.web-server-url:http://120.78.1.49}")
    private String webServerUrl;

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 1000;

    @Retryable(value = {SocketTimeoutException.class, IOException.class}, 
               maxAttempts = MAX_RETRY_ATTEMPTS, 
               backoff = @Backoff(delay = RETRY_DELAY_MS, multiplier = 2))
    public String uploadFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }
        
        LogUtils.info("开始上传文件: " + file.getOriginalFilename() + ", 大小: " + file.getSize() + " bytes");
        
        try {
            StorePath storePath = storageClient.uploadFile(
                file.getInputStream(), 
                file.getSize(),
                getFileExtension(file.getOriginalFilename()), 
                null
            );
            
            String fullPath = storePath.getFullPath();
            String completeUrl = buildCompleteUrl(fullPath);
            LogUtils.info("文件上传成功，相对路径: " + fullPath + ", 完整URL: " + completeUrl);
            return completeUrl;
            
        } catch (SocketTimeoutException e) {
            LogUtils.error("FastDFS连接超时，正在重试... 错误信息: " + e.getMessage());
            throw e;
        } catch (IOException e) {
            LogUtils.error("文件上传IO异常: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LogUtils.error("文件上传未知异常: " + e.getMessage(), e);
            throw new IOException("文件上传失败: " + e.getMessage(), e);
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            LogUtils.error("upload fileName is Empty, check please!!");
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            LogUtils.error("文件扩展名无效: " + fileName);
            return "";
        }
        return fileName.substring(lastDotIndex + 1);
    }

    /**
     * 构建完整的文件访问URL
     * @param relativePath FastDFS返回的相对路径，如：group1/M00/00/00/eE4BMWjGSaWAWr5XAACogd96-1w75.jpeg
     * @return 完整的访问URL，如：http://120.78.1.49/group1/M00/00/00/eE4BMWjGSaWAWr5XAACogd96-1w75.jpeg
     */
    private String buildCompleteUrl(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            LogUtils.error("相对路径为空，无法构建完整URL");
            return "";
        }
        
        // 确保webServerUrl以/结尾，relativePath不以/开头
        String baseUrl = webServerUrl.endsWith("/") ? webServerUrl : webServerUrl + "/";
        String path = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
        
        String completeUrl = baseUrl + path;
        LogUtils.info("构建完整URL: " + completeUrl);
        return completeUrl;
    }
}
