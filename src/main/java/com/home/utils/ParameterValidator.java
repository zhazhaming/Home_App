package com.home.utils;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @Author: zhazhaming
 * @Date: 2024/09/12/23:56
 */
@Component
public class ParameterValidator {

    // 验证 String 类型参数是否合法
    public static boolean validateString(String value) {
        if (StringUtils.isEmpty (value) || value.trim().isEmpty()) {
            return false;
        }
        return true;
    }
}
