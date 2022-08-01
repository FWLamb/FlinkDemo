package com.yang.flink.transformation;

import com.yang.flink.pojo.Event;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author Bin
 * @date 2022/4/13 9:51
 * @Description
 */
public class PojoAggregationTransTest {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        DataStreamSource<Event> stream = env.fromElements(
                new Event("Mary", "./home", 1000L),
                new Event("Bob", "./cart", 2000L),
                new Event("Mary", "./home", 3000L),
                new Event("Bob", "./home", 4000L)
        );


        // 按名称参数求和
        stream.keyBy(t -> t.user).sum("timestamp").print("sum");

        stream.keyBy(r -> r.user).max("timestamp").print("max");

        stream.keyBy(r -> r.user).min("timestamp").print("min");

        stream.keyBy(r -> r.user).maxBy("timestamp").print("maxBy");

        stream.keyBy(r -> r.user).minBy("timestamp").print("minBy");

        env.execute();

    }
}
