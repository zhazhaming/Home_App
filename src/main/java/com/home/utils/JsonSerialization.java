package com.home.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import org.springframework.stereotype.Component;

/**
 * @Author: zhazhaming
 * @Date: 2024/08/06/22:20
 */
@Component
public class JsonSerialization {

    public String serializeToJson(Object object) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // 注册JavaTimeModule以支持LocalDateTime等Java8时间类型
        mapper.registerModule(new JavaTimeModule());
        // 禁用将日期写为时间戳的功能
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper.writeValueAsString(object);
    }

//    public <T> T deserializeFromJson(String json, Class<T> clazz) throws Exception {
//        ObjectMapper mapper = new ObjectMapper ();
//        return mapper.readValue(json, clazz);
//    }

    /**
     * 反序列化方法（支持Java8日期时间类型）
     * 专门用于反序列化包含LocalDateTime等Java8时间类型的DTO
     * 
     * @param json JSON字符串
     * @param clazz 目标类型
     * @return 反序列化后的对象
     * @throws Exception 反序列化异常
     */
    public <T> T deserializeFromJson(String json, Class<T> clazz) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        // 注册JavaTimeModule以支持LocalDateTime等Java8时间类型
        mapper.registerModule(new JavaTimeModule());
        
        // 禁用将日期写为时间戳的功能
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // 忽略未知属性，防止反序列化失败
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        return mapper.readValue(json, clazz);
    }

}
