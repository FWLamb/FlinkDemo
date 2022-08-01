package com.yang.flink.transformation;

import com.yang.flink.pojo.Event;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * @author Bin
 * @date 2022/4/13 9:24
 * @Description
 */
public class FlatMapTransTest {
    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStreamSource<Event> dataStreamSource = env.fromElements(new Event("Bob", "./home", 1000L),
                new Event("Tom", "./usr", 2000L));


        dataStreamSource.flatMap(new MyFlatMap()).print();

        // 执行
        env.execute();

    }


    private static class MyFlatMap implements FlatMapFunction<Event, String> {
        @Override
        public void flatMap(Event event, Collector<String> collector) throws Exception {
            if (event.user.equals("Bob")) {
                collector.collect(event.user);
                collector.collect(event.url);
                collector.collect(String.valueOf(event.timestamp));
            } else {
                collector.collect(event.user);
            }

        }
    }
}
