package com.yang.flink.watermark;

import com.yang.flink.pojo.Event;
import com.yang.flink.source.ClickSource;
import org.apache.flink.api.common.eventtime.*;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author Bin
 * @date 2022/4/25 10:49
 * @Description 自定义断点式水位线策略
 */
public class CustomPunctuatedWaterMark {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        env.addSource(new ClickSource()).assignTimestampsAndWatermarks(new CustomWaterMarkStrategy()).print();

        env.execute();

    }

    private static class CustomWaterMarkStrategy implements WatermarkStrategy<Event> {
        @Override
        public WatermarkGenerator<Event> createWatermarkGenerator(WatermarkGeneratorSupplier.Context context) {
            return new CustomPunctuatedGenerator();
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

        private class CustomPunctuatedGenerator implements WatermarkGenerator<Event> {

            @Override
            public void onEvent(Event event, long l, WatermarkOutput watermarkOutput) {
                // 只有在遇到特定的 itemId 时，才发出水位线
                if (event.user.equals("Mary")) {
                    watermarkOutput.emitWatermark(new Watermark(event.timestamp - 1));
                }
            }

            @Override
            public void onPeriodicEmit(WatermarkOutput watermarkOutput) {
                // 不需要做任何事情，因为我们在 onEvent 方法中发射了水位线
            }
        }
    }
}

