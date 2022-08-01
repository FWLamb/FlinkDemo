package com.yang.flink.sink;

import com.yang.flink.pojo.Event;
import com.yang.flink.source.ClickSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.redis.RedisSink;
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommand;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommandDescription;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisMapper;


/**
 * @author Bin
 * @date 2022/4/16 21:48
 * @Description
 */
public class SinkToRedisTest {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        FlinkJedisPoolConfig conf = new FlinkJedisPoolConfig.Builder().setHost("192.168.10.102").build();

        env.addSource(new ClickSource())
                .addSink(new RedisSink<Event>(conf, new MyRedisMapper()));

        env.execute();

    }

    private static class MyRedisMapper implements RedisMapper<Event> {
        @Override
        public RedisCommandDescription getCommandDescription() {
            return new RedisCommandDescription(RedisCommand.HSET, "click2");
        }

        @Override
        public String getKeyFromData(Event event) {
            return event.user;
        }

        @Override
        public String getValueFromData(Event event) {
            return event.url;
        }
    }
}
