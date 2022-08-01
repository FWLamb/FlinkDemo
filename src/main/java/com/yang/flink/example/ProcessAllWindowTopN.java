package com.yang.flink.example;

import com.yang.flink.pojo.Event;
import com.yang.flink.source.ClickSource;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Bin
 * @date 2022/4/26 17:00
 * @Description
 */
public class ProcessAllWindowTopN {
    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        SingleOutputStreamOperator<Event> eventStream = env.addSource(new ClickSource())
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<Event>forBoundedOutOfOrderness(Duration.ZERO)
                                .withTimestampAssigner(
                                        (SerializableTimestampAssigner<Event>) (event, l) -> event.timestamp));

        // 只需要 url 就可以统计数量，所以转换成 String 直接开窗统计
        SingleOutputStreamOperator<String> result = eventStream.map((MapFunction<Event, String>) value -> value.url)
                .windowAll(SlidingEventTimeWindows.of(Time.seconds(10), Time.seconds(5)))
                .process(new ProcessAllWindowFunction<String, String, TimeWindow>() {
                    @Override
                    public void process(Context context, Iterable<String> elements, Collector<String> out) throws Exception {
                        HashMap<String, Long> urlCountMap = new HashMap<>();
                        // 遍历窗口中数据，将浏览量保存到一个 HashMap 中
                        for (String element : elements) {
                            if (urlCountMap.containsKey(element)) {
                                Long aLong = urlCountMap.get(element);
                                urlCountMap.put(element, aLong + 1L);
                            } else {
                                urlCountMap.put(element, 1L);
                            }
                        }
                        ArrayList<Tuple2<String, Long>> arrayList = new ArrayList<>();
                        // 将浏览量数据放入 ArrayList，进行排序
                        for (String key : urlCountMap.keySet()) {
                            arrayList.add(Tuple2.of(key, urlCountMap.get(key)));
                        }
                        arrayList.sort((o1, o2) -> o2.f1.intValue() - o1.f1.intValue());

                        // 取排序后的前两名，构建输出结果
                        StringBuilder result = new StringBuilder();

                        result.append("========================================\n");
                        for (int i = 0; i < 2; i++) {
                            Tuple2<String, Long> temp = arrayList.get(i);
                            String info = "浏览量 No." + (i + 1) +
                                    " url：" + temp.f0 +
                                    " 浏览量：" + temp.f1 +
                                    " 窗 口 结 束 时 间 ： " + new
                                    Timestamp(context.window().getEnd()) + "\n";
                            result.append(info);
                        }

                        result.append("========================================\n");
                        out.collect(result.toString());
                    }
                });
        result.print();
        env.execute();

    }
}
