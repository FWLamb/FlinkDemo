package com.yang.flink.streamconversion;

import com.yang.flink.pojo.Event;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

import java.time.Duration;

/**
 * @author Bin
 * @date 2022/4/27 16:42
 * @Description
 */
public class UnionExample {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        SingleOutputStreamOperator<Event> stream1 = env.socketTextStream("localhost", 7777)
                .map(data -> {
                    String[] field = data.split(",");
                    return new Event(field[0].trim(), field[1].trim(), Long.valueOf(field[2].trim()));
                }).assignTimestampsAndWatermarks(
                        WatermarkStrategy.<Event>forBoundedOutOfOrderness(Duration.ofSeconds(2))
                                .withTimestampAssigner((SerializableTimestampAssigner<Event>) (event, along) -> event.timestamp));
        stream1.print("stream1");

        SingleOutputStreamOperator<Event> stream2 = env.socketTextStream("localhost", 8888)
                .map(data -> {
                    String[] field = data.split(",");
                    return new Event(field[0].trim(), field[1].trim(), Long.valueOf(field[2].trim()));
                }).assignTimestampsAndWatermarks(
                        WatermarkStrategy.<Event>forBoundedOutOfOrderness(Duration.ofSeconds(5))
                                .withTimestampAssigner((SerializableTimestampAssigner<Event>) (event, along) -> event.timestamp));
        stream2.print("stream2");

        // 合并两条流
        stream1.union(stream2)
                .process(new ProcessFunction<Event, String>() {
                    @Override
                    public void processElement(Event value, Context ctx, Collector<String> out) throws Exception {
                        out.collect(" 水 位 线 ： " + ctx.timerService().currentWatermark());
                    }
                }).print("union");

        env.execute();


    }
}
