package com.yang.flink.watermark;

import com.yang.flink.pojo.Event;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.time.Duration;

/**
 * @author Bin
 * @date 2022/4/25 10:19
 * @Description
 */
public class InternalWaterMark {
    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        // 设置水位线生成的时间间隔
        env.getConfig().setAutoWatermarkInterval(100);

        DataStream<Event> stream = env.fromElements(
                        new Event("Mary", "./home", 1000L),
                        new Event("Bob", "./cart", 2000L),
                        new Event("Alice", "./prod?id=100", 3000L),
                        new Event("Alice", "./prod?id=200", 3500L),
                        new Event("Bob", "./prod?id=2", 2500L),
                        new Event("Alice", "./prod?id=300", 3600L),
                        new Event("Bob", "./prod?id=1", 2300L),
                        new Event("Bob", "./prod?id=3", 3300L))
                // 有序流的watermark生成
                //.assignTimestampsAndWatermarks(WatermarkStrategy.<Event>forMonotonousTimestamps()
                //        .withTimestampAssigner(new SerializableTimestampAssigner<Event>() {
                //            @Override
                //            public long extractTimestamp(Event event, long l) {
                //                return event.timestamp;
                //            }
                //        }));
                // 无序流
                .assignTimestampsAndWatermarks(WatermarkStrategy.<Event>forBoundedOutOfOrderness(Duration.ofSeconds(2))
                        .withTimestampAssigner(new SerializableTimestampAssigner<Event>() {
                            @Override
                            public long extractTimestamp(Event event, long l) {
                                return event.timestamp;
                            }
                        }));

        env.execute();

    }
}
