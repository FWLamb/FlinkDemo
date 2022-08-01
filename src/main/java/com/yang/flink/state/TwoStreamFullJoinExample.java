package com.yang.flink.state;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.util.Collector;

/**
 * @author Bin
 * @date 2022/4/28 16:19
 * @Description
 */
public class TwoStreamFullJoinExample {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        SingleOutputStreamOperator<Tuple3<String, String, Long>> stream1 = env.fromElements(
                        Tuple3.of("a", "stream-1", 1000L),
                        Tuple3.of("b", "stream-1", 2000L))
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<Tuple3<String, String, Long>>forMonotonousTimestamps()
                                .withTimestampAssigner((value, along) -> value.f2));

        SingleOutputStreamOperator<Tuple3<String, String, Long>> stream2 = env.fromElements(
                        Tuple3.of("a", "stream-2", 3000L),
                        Tuple3.of("b", "stream-2", 4000L))
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<Tuple3<String, String, Long>>forMonotonousTimestamps()
                                .withTimestampAssigner((value, along) -> value.f2));
        stream1.connect(stream2)
                .keyBy(r -> r.f0, r -> r.f0)
                .process(new CoProcessFunction<Tuple3<String, String, Long>, Tuple3<String, String, Long>, String>() {

                    private ListState<Tuple3<String, String, Long>> stream1ListState;
                    private ListState<Tuple3<String, String, Long>> stream2ListState;

                    @Override
                    public void open(Configuration parameters) throws Exception {
                        stream1ListState = getRuntimeContext().getListState(
                                new ListStateDescriptor<>("stream1-list", Types.TUPLE(Types.STRING, Types.STRING))
                        );
                        stream2ListState = getRuntimeContext().getListState(
                                new ListStateDescriptor<>("stream2-list", Types.TUPLE(Types.STRING, Types.STRING))
                        );
                    }

                    @Override
                    public void onTimer(long timestamp, OnTimerContext ctx, Collector<String> out) throws Exception {
                        super.onTimer(timestamp, ctx, out);
                    }

                    @Override
                    public void processElement1(Tuple3<String, String, Long> value1, Context ctx,
                                                Collector<String> out) throws Exception {
                        stream1ListState.add(value1);
                        for (Tuple3<String, String, Long> value2 : stream2ListState.get()) {
                            out.collect(value1 + "=>" + value2);
                        }
                    }

                    @Override
                    public void processElement2(Tuple3<String, String, Long> value2, Context ctx,
                                                Collector<String> out) throws Exception {
                        stream2ListState.add(value2);
                        for (Tuple3<String, String, Long> value1 : stream2ListState.get()) {
                            out.collect(value1 + "=>" + value2);
                        }
                    }
                })
                .print();

        env.execute();
    }
}
