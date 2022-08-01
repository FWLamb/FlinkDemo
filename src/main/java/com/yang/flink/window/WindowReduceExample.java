package com.yang.flink.window;

import com.yang.flink.pojo.Event;
import com.yang.flink.source.ClickSource;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.time.Duration;

/**
 * @author Bin
 * @date 2022/4/25 14:41
 * @Description
 */
public class WindowReduceExample {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        // 从自定义数据源读取数据 并提取时间戳 生成水位线
        SingleOutputStreamOperator<Event> stream = env.addSource(new ClickSource())
                .assignTimestampsAndWatermarks(WatermarkStrategy.<Event>forBoundedOutOfOrderness(Duration.ZERO)
                        .withTimestampAssigner((SerializableTimestampAssigner<Event>) (event, l) -> event.timestamp));
        stream.map((MapFunction<Event, Tuple2<String, Long>>) event -> {
                    // 将数据转成二元组 方便计算
                    return Tuple2.of(event.user, 1L);
                })
                .keyBy(r -> r.f0)
                // 设置滚动事件窗口
                .window(TumblingEventTimeWindows.of(Time.seconds(5)))
                .reduce((ReduceFunction<Tuple2<String, Long>>) (t1, t2) -> {
                    // 定义累加规则，窗口闭合时，向下游发送累加结果
                    return Tuple2.of(t1.f0, t1.f1 + t1.f1);
                }).print();
        env.execute();
    }
}
