package com.yang.flink.state;

import com.yang.flink.pojo.Event;
import com.yang.flink.source.ClickSource;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.sql.Timestamp;

/**
 * @author Bin
 * @date 2022/4/28 16:36
 * @Description 使用 KeyedProcessFunction 模拟滚动窗口
 */
public class FakeWindowExample {
    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env =
                StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        SingleOutputStreamOperator<Event> stream = env.addSource(new
                        ClickSource())
                .assignTimestampsAndWatermarks(WatermarkStrategy.<Event>forMonotonousTimestamps()
                        .withTimestampAssigner((element, recordTimestamp) -> element.timestamp)
                );
        //   统计每 10s 窗口内，每个 url 的 pv
        stream.keyBy(data -> data.url)
                .process(new FakeWindowResult(10000L))
                .print();

        env.execute();

    }

    private static class FakeWindowResult extends KeyedProcessFunction<String, Event, String> {
        // 定义属性，窗口长度
        private Long windowSize;

        // 声明状态，用 map 保存 pv 值（窗口 start，count）
        MapState<Long, Long> windowPvMapState;

        public FakeWindowResult(Long windowSize) {
            this.windowSize = windowSize;
        }

        @Override
        public void open(Configuration parameters) throws Exception {
            windowPvMapState = getRuntimeContext().getMapState(
                    new MapStateDescriptor<>("window-pv", Long.class, Long.class));
        }

        @Override
        public void processElement(Event value, Context ctx, Collector<String> out) throws Exception {
            // 每来一条数据，就根据时间戳判断属于哪个窗口
            Long windowStart = value.timestamp / windowSize * windowSize;
            Long windowEnd = windowStart + windowSize;

            // 注册 end -1 的定时器，窗口触发计算
            ctx.timerService().registerEventTimeTimer(windowEnd - 1L);

            // 更新状态中的 pv 值
            if (windowPvMapState.contains(windowStart)) {
                Long pv = windowPvMapState.get(windowStart);
                windowPvMapState.put(windowStart, ++pv);
            } else {
                windowPvMapState.put(windowStart, 1L);
            }
        }

        // 定时器触发，直接输出统计的 pv 结果
        @Override
        public void onTimer(long timestamp, OnTimerContext ctx, Collector<String> out) throws Exception {
            Long windowEnd = timestamp + 1;
            Long windowStart = windowEnd - windowSize;
            Long pv = windowPvMapState.get(windowStart);
            out.collect("url: " + ctx.getCurrentKey()
                    + " 访问量: " + pv
                    + " 窗 口 ： " + new Timestamp(windowStart) + " ~ " + new Timestamp(windowEnd));
            // 模拟窗口的销毁，清除 map 中的 key
            windowPvMapState.remove(windowStart);
        }
    }
}
