package com.home.Enum;

/**
 * @Author: zhazhaming
 * @Date: 2024/06/09/11:22
 */
public enum PageEnum {

    PAGE_MOVICE(5,10,"电影列表");

    private Integer pageNumber;

    private Integer pageSize;

    private String msg;

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    PageEnum(Integer pageNumber, Integer pageSize, String msg) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.msg = msg;
    }
}
