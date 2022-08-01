package com.yang.flink.streamconversion;

import com.yang.flink.pojo.Event;
import com.yang.flink.source.ClickSource;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author Bin
 * @date 2022/4/27 16:20
 * @Description
 */
public class SplitStreamByFilter {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //env.setParallelism(1);

        DataStreamSource<Event> stream = env.addSource(new ClickSource());
        // 筛选 Mary 的浏览行为放入 MaryStream 流中
        SingleOutputStreamOperator<Event> MaryStream = stream.filter(
                (FilterFunction<Event>) value -> value.user.equals("Mary"));

        // 筛选 Bob 的购买行为放入 BobStream 流中
        SingleOutputStreamOperator<Event> BobStream = stream.filter(
                (FilterFunction<Event>) value -> value.user.equals("Bob"));

        // 筛选其他人的浏览行为放入 otherStream 流中
        SingleOutputStreamOperator<Event> otherStream = stream.filter(
                (FilterFunction<Event>) value -> !(value.user.equals("Mary") || value.user.equals("Bob")));

        MaryStream.print("Mary");
        BobStream.print("Bob");
        otherStream.print("other");

        env.execute();
    }
}
