package com.yang.flink.watermark;

import com.yang.flink.pojo.Event;
import com.yang.flink.source.ClickSource;
import org.apache.flink.api.common.eventtime.*;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author Bin
 * @date 2022/4/25 10:49
 * @Description 自定义周期性水位线策略
 */
public class CustomPeriodicWaterMark {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        env.addSource(new ClickSource())
                .assignTimestampsAndWatermarks(new CustomWaterMarkStrategy())
                .print();

        env.execute();

    }

    private static class CustomWaterMarkStrategy implements WatermarkStrategy<Event> {
        @Override
        public WatermarkGenerator<Event> createWatermarkGenerator(WatermarkGeneratorSupplier.Context context) {
            return new CustomPeriodicGenerator();
        }

        @Override
        public TimestampAssigner<Event> createTimestampAssigner(TimestampAssignerSupplier.Context context) {
            return new SerializableTimestampAssigner<Event>() {
                @Override
                public long extractTimestamp(Event event, long l) {
                    return event.timestamp;  // 告诉程序数据源里的时间戳是哪一个字段
                }
            };
        }

        private class CustomPeriodicGenerator implements WatermarkGenerator<Event> {
            // 延迟时间
            private Long delayTime = 5000L;
            // 观察到的最大时间戳
            private Long maxTs = Long.MIN_VALUE + delayTime + 1L;

            @Override
            public void onEvent(Event event, long l, WatermarkOutput watermarkOutput) {
                // 每来一条数据就调用一次
                maxTs = Math.max(event.timestamp, maxTs);  // 更新最大时间戳
            }

            @Override
            public void onPeriodicEmit(WatermarkOutput watermarkOutput) {
                // 发射水位线，默认200ms调用一次
                watermarkOutput.emitWatermark(new Watermark(maxTs - delayTime - 1L));
            }
        }
    }
}

