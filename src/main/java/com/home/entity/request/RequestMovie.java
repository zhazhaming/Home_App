package com.home.entity.request;

import com.home.entity.BO.MoviceBo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author: zhazhaming
 * @Date: 2024/05/03/16:15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestMovie {
    public List<MoviceBo> moviceBoList;
}
