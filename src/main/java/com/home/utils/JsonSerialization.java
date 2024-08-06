package com.home.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

/**
 * @Author: zhazhaming
 * @Date: 2024/08/06/22:20
 */
@Component
public class JsonSerialization {

    public String serializeToJson(Object object) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }

    public <T> T deserializeFromJson(String json, Class<T> clazz) throws Exception {
        ObjectMapper mapper = new ObjectMapper ();
        return mapper.readValue(json, clazz);
    }

}
