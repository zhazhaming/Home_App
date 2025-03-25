package com.home.entity.BO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: zhazhaming
 * @Date: 2024/05/03/15:54
 * 业务对象，通常与数据库对应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoviceBo {

    private String name;

    private String url;
}
