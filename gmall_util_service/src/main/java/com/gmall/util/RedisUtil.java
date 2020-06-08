package com.gmall.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Redis Util Class for Service layer
 */
public class RedisUtil {

    private JedisPool jedisPool;

    public void initJedisPool(String host, int port, int database){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();

        //pool --> JDBC pool
        jedisPoolConfig.setMaxTotal(200);
        jedisPoolConfig.setMaxWaitMillis(2000);
        jedisPoolConfig.setMinIdle(10);
        jedisPoolConfig.setBlockWhenExhausted(true);
        jedisPoolConfig.setTestOnBorrow(true); //得到connection之后先ping一下，确认连接好的

        jedisPool = new JedisPool(jedisPoolConfig,host, port, 5000);
    }

    public Jedis getJedis(){
        Jedis jedis =  jedisPool.getResource();
        return jedis;
    }




}
