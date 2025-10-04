package com.home.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.home.entity.DTO.ReservationRequestDTO;
import com.home.entity.DTO.CancelReservationRequestDTO;
import com.home.entity.Reservation;
import io.swagger.models.auth.In;

import java.util.List;

public interface ReservationService extends IService<Reservation> {

    /**
     * 发送预约消息；若电影已存在则不发送并返回 false。
     * @return true 表示已入队；false 表示电影已存在，未入队
     */
    boolean sendReservationMessage(ReservationRequestDTO request) throws Exception;

    /**
     * 发送取消预约消息；若预约不存在或已取消则不发送并返回 false。
     * @return true 表示已入队；false 表示预约不存在或已取消，未入队
     */
    boolean sendCancelReservationMessage(CancelReservationRequestDTO request) throws Exception;

    /**
     * 消费预约消息：当有新电影上线时，处理与该标题匹配的预约。
     * @param onlineTitle 新上线电影标题
     * @return 处理成功的预约数量
     */
    Integer consumeReservationForTitle(String onlineTitle) throws Exception;

    List<Reservation> getReservationPageById(Integer userId, Integer page, Integer pageSize) throws Exception;

    Integer getTotalReservation(Integer userId);

    /**
     * 发送电影上线通知
     * @param title 电影标题
     * @param movieId 电影ID
     * @return 发送通知数量
     */
    Integer sendMovieOnlineNotifications(String title, Integer movieId) throws Exception;
}


