package com.yang.flink.transformation;

import com.yang.flink.pojo.Event;
import com.yang.flink.source.ClickSource;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author Bin
 * @date 2022/4/13 10:08
 * @Description
 */
public class ReduceTransTest {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        env.addSource(new ClickSource()).map(
                        // 将Event类型转为二元组类型
                        new MapFunction<Event, Tuple2<String, Long>>() {
                            @Override
                            public Tuple2<String, Long> map(Event event) throws Exception {
                                return Tuple2.of(event.user, 1L);
                            }
                        }
                ).keyBy(e -> e.f0) //根据用户名分区
                .reduce((t1, t2) -> Tuple2.of(t1.f0, t1.f1 + t2.f1)) // 每到一条数据，用户 pv 的统计值加 1
                .keyBy(r -> true) // 为每一条数据分配同一个 key，将聚合结果发送到一条流中去
                .reduce(new ReduceFunction<Tuple2<String, Long>>() {
                    @Override
                    public Tuple2<String, Long> reduce(Tuple2<String, Long> t1,
                                                       Tuple2<String, Long> t2) throws Exception {
                        // 将累加器更新为当前最大的 pv 统计值，然后向下游发送累加器的值
                        return t1.f1 > t2.f1 ? t1 : t2;
                    }
                }).print();

        env.execute();

    }
}
