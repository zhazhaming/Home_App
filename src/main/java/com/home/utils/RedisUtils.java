package com.home.utils;

import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author: zhazhaming
 * @Date: 2024/08/05/21:50
 */
@Component
public class RedisUtils {
    @Autowired
    private StringRedisTemplate redisTemplate;

    // Key（键），简单的key-value操作

    /**
     * 判断key是否存在
     * @param key
     * @return
     */
    public boolean keyIsExist(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 实现命令：TTL key，以秒为单位，返回给定 key的剩余生存时间(TTL, time to live)。
     *
     * @param key
     * @return
     */
    public long ttl(String key) {
        return redisTemplate.getExpire(key);
    }

    /**
     * 实现命令：expire 设置过期时间，单位秒
     *
     * @param key
     * @return
     */
    public void expire(String key, long timeout) {
        redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * 实现命令：increment key，增加key一次
     *
     * @param key
     * @return
     */
    public long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 实现命令：decrement key，减少key一次
     *
     * @param key
     * @return
     */
    public long decrement(String key, long delta) {
        return redisTemplate.opsForValue().decrement(key, delta);
    }

    /**
     * 实现命令：KEYS pattern，查找所有符合给定模式 pattern的 key
     */
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    /**
     * 实现命令：DEL key，删除一个key
     *
     * @param key
     */
    public void del(String key) {
        redisTemplate.delete(key);
    }

    // String（字符串）

    /**
     * 实现命令：SET key value，设置一个key-value（将字符串值 value关联到 key）
     *
     * @param key
     * @param value
     */
    public void set(String key, Object value) {
        if (value instanceof String){
            redisTemplate.opsForValue().set(key, (String) value);
        }else if(value instanceof Integer){
            redisTemplate.opsForValue ().set (key, value.toString ());
        }else {
            throw new IllegalArgumentException("Unsupported type: " + value.getClass().getName());
        }
    }

    /**
     * 实现命令：SET key value EX seconds，设置key-value和超时时间（秒）
     *
     * @param key
     * @param value
     * @param timeout
     *            （以秒为单位）
     */
    public void set(String key, String value, long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }

    /**
     * 如果key不存在，则设置，如果存在，则报错
     * @param key
     * @param value
     */
    public void setnx60s(String key, String value) {
        redisTemplate.opsForValue().setIfAbsent(key, value, 60, TimeUnit.SECONDS);
    }

    /**
     * 如果key不存在，则设置，如果存在，则报错
     * @param key
     * @param value
     */
    public void setnx(String key, String value) {
        redisTemplate.opsForValue().setIfAbsent(key, value);
    }

    /**
     * 实现命令：GET key，返回 key所关联的字符串值。
     *
     * @param key
     * @return value
     */
    public String get(String key) {
        return (String)redisTemplate.opsForValue().get(key);
    }

    /**
     * 批量查询，对应mget
     * @param keys
     * @return
     */
    public List<String> mget(List<String> keys) {
        return redisTemplate.opsForValue().multiGet(keys);
    }

    /**
     * 批量查询，管道pipeline
     * @param keys
     * @return
     */
    public List<Object> batchGet(List<String> keys) {

//		nginx -> keepalive
//		redis -> pipeline

        List<Object> result = redisTemplate.executePipelined(new RedisCallback<String> () {
            @Override
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                StringRedisConnection src = (StringRedisConnection)connection;

                for (String k : keys) {
                    src.get(k);
                }
                return null;
            }
        });
        return result;
    }


    // Hash（哈希表）

    /**
     * 实现命令：HSET key field value，将哈希表 key中的域 field的值设为 value
     *
     * @param key
     * @param field
     * @param value
     */
    public void hset(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    /**
     * 实现命令：HGET key field，返回哈希表 key中给定域 field的值
     *
     * @param key
     * @param field
     * @return
     */
    public String hget(String key, String field) {
        return (String) redisTemplate.opsForHash().get(key, field);
    }

    /**
     * 实现命令：HDEL key field [field ...]，删除哈希表 key 中的一个或多个指定域，不存在的域将被忽略。
     *
     * @param key
     * @param fields
     */
    public void hdel(String key, Object... fields) {
        redisTemplate.opsForHash().delete(key, fields);
    }

    /**
     * 实现命令：HGETALL key，返回哈希表 key中，所有的域和值。
     *
     * @param key
     * @return
     */
    public Map<Object, Object> hgetall(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    // List（列表）

    /**
     * 实现命令：LPUSH key value，将一个值 value插入到列表 key的表头
     *
     * @param key
     * @param value
     * @return 执行 LPUSH命令后，列表的长度。
     */
    public long lpush(String key, String value) {
        return redisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * 实现命令：LPOP key，移除并返回列表 key的头元素。
     *
     * @param key
     * @return 列表key的头元素。
     */
    public String lpop(String key) {
        return (String)redisTemplate.opsForList().leftPop(key);
    }

    /**
     * 实现命令：RPUSH key value，将一个值 value插入到列表 key的表尾(最右边)。
     *
     * @param key
     * @param value
     * @return 执行 LPUSH命令后，列表的长度。
     */
    public long rpush(String key, String value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    //set和zset

    /**
     * 添加zset有序集合中的元素
     * @param key
     * @param value
     * @param score
     */
    public void szset(String key, String value, double score){
        ZSetOperations<String, String> opsForZSet = redisTemplate.opsForZSet();
        opsForZSet.add(key, value, score);
    }

    /**
     * 移除zset中的元素
     * @param key
     * @param value
     */
    public void rzset(String key, String value){
        ZSetOperations<String, String> opsForZSet = redisTemplate.opsForZSet ( );
        opsForZSet.remove (key,value);
    }

    /**
     * 更新zset中的分数
     * @param key
     * @param value
     * @param delta
     */
    public void incrementZset(String key, String value, double delta){
        ZSetOperations<String, String> opsForZSet = redisTemplate.opsForZSet();
        Double currentScore = opsForZSet.score(key, value);
        if (currentScore != null) {
            double newScore = currentScore + delta;
            opsForZSet.remove(key, value); // 移除旧的成员
            opsForZSet.add(key, value, newScore); // 添加新的分数
        }
    }

    /**
     * 检查zset集合中是否包含指定成员。
     * @param key
     * @param value
     * @return
     */
    public boolean containsZsetValue(String key, String value){
        ZSetOperations<String, String> opsForZSet = redisTemplate.opsForZSet();
        Double score = opsForZSet.score(key, value);
        return score != null;
    }

    /**
     * 获取zset集合中指定成员的分数。
     * @param key
     * @param value
     * @return
     */
    public Double getZsetValue(String key, String value){
        ZSetOperations<String, String> opsForZSet = redisTemplate.opsForZSet();
        Double score = opsForZSet.score(key, value);
        return score;
    }

    /**
     * 获取zset中从低到高的元素排列
     * @param key
     * @param start
     * @param end
     * @return
     */
    public Set<String> gzsetReOrder(String key, long start, long end){
        ZSetOperations<String, String> opsForZSet = redisTemplate.opsForZSet();
        return opsForZSet.range(key, start, end);
    }

    /**
     * 获取zset中从高到低的元素排列
     * @param key
     * @param start
     * @param end
     * @return
     */
    public Set<String > gzsetOrder(String key, long start, long end){
        ZSetOperations<String, String> opsForZSet = redisTemplate.opsForZSet();
        return opsForZSet.reverseRange (key, start, end);
    }

    /**
     * 向set集合中添加成员。
     *
     * @param key   集合的键
     * @param value 成员值
     */
    public void sset(String key, String value) {
        SetOperations<String, String> opsForSet = redisTemplate.opsForSet();
        opsForSet.add(key, value);
    }

    /**
     * 从set集合中移除成员。
     *
     * @param key   集合的键
     * @param value 成员值
     */
    public void rset(String key, String value) {
        SetOperations<String, String> opsForSet = redisTemplate.opsForSet();
        opsForSet.remove(key, value);
    }

    /**
     * 获取set集合中的所有成员。
     *
     * @param key 集合的键
     * @return 成员集合
     */
    public Set<String> gset(String key) {
        SetOperations<String, String> opsForSet = redisTemplate.opsForSet();
        return opsForSet.members(key);
    }
}
