package com.yang.flink.transformation;

import com.yang.flink.pojo.Event;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author Bin
 * @date 2022/4/13 9:24
 * @Description
 */
public class MapTransTest {
    public static void main(String[] args) throws Exception {
        /**
         * 通过map获取Event里的usr
         */
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStreamSource<Event> dataStreamSource = env.fromElements(new Event("Bob", "./home", 1000L),
                new Event("Tom", "./usr", 2000L));

        // 1. 第一种使用方式 匿名类实现MapFunction接口
        dataStreamSource.map((MapFunction<Event, String>) event -> event.user).print("one_");

        // 2. 第二种使用方式 传入MapFunction接口的实现类
        dataStreamSource.map(new UserExtractor()).print("two_");

        // 执行
        env.execute();

    }

    public static class UserExtractor implements MapFunction<Event, String> {
        @Override
        public String map(Event event) throws Exception {
            return event.user;
        }
    }
}
