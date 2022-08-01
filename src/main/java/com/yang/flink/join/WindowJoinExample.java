package com.yang.flink.join;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.time.Duration;

/**
 * @author Bin
 * @date 2022/4/28 14:25
 * @Description
 */
public class WindowJoinExample {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        SingleOutputStreamOperator<Tuple2<String, Long>> stream1 = env.fromElements(
                Tuple2.of("a", 1000L),
                Tuple2.of("b", 1000L),
                Tuple2.of("a", 2000L),
                Tuple2.of("b", 2000L)
        ).assignTimestampsAndWatermarks(
                WatermarkStrategy.<Tuple2<String, Long>>forBoundedOutOfOrderness(Duration.ZERO)
                        .withTimestampAssigner((tuple, along) -> tuple.f1));

        SingleOutputStreamOperator<Tuple2<String, Long>> stream2 = env.fromElements(
                Tuple2.of("a", 3000L),
                Tuple2.of("b", 3000L),
                Tuple2.of("a", 4000L),
                Tuple2.of("b", 4000L),
                Tuple2.of("a", 4999L),
                Tuple2.of("a", 5000L)
        ).assignTimestampsAndWatermarks(
                WatermarkStrategy.<Tuple2<String, Long>>forBoundedOutOfOrderness(Duration.ofSeconds(1L))
                        .withTimestampAssigner((tuple, along) -> tuple.f1));

        stream1.join(stream2)
                .where(t -> t.f0)
                .equalTo(t -> t.f0)
                .window(TumblingEventTimeWindows.of(Time.seconds(5L)))
                .allowedLateness(Time.seconds(1L))
                .apply((var1, var2) -> var1 + "=>" + var2)
                .print();


        env.execute();
    }
}
