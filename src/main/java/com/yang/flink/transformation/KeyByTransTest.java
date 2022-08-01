package com.yang.flink.transformation;

import com.yang.flink.pojo.Event;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author Bin
 * @date 2022/4/13 9:46
 * @Description
 */
public class KeyByTransTest {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStreamSource<Event> dataStreamSource = env.fromElements(new Event("Bob", "./home", 1000L),
                new Event("Tom", "./usr", 2000L));

        dataStreamSource.keyBy(new KeySelector<Event, String>() {
            @Override
            public String getKey(Event event) throws Exception {
                return event.user;
            }
        }).print();

        KeyedStream<Event, String> keyedStream = dataStreamSource.keyBy(e -> e.user);
        keyedStream.print();

        env.execute();
    }
}
