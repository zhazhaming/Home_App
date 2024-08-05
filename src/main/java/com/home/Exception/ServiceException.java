package com.home.Exception;

/**
 * @Author: zhazhaming
 * @Date: 2024/07/30/22:17
 */
public class ServiceException extends RuntimeException{

    private final int errorCode;

    private final String errorMessage;

    public ServiceException(int errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
