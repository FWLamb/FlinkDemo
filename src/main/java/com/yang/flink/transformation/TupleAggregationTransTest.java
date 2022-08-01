package com.yang.flink.transformation;

import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author Bin
 * @date 2022/4/13 9:51
 * @Description
 */
public class TupleAggregationTransTest {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        DataStreamSource<Tuple2<String, Integer>> stream = env.fromElements(
                Tuple2.of("a", 1), Tuple2.of("a", 3),
                Tuple2.of("b", 3), Tuple2.of("b", 4)
        );

        // 按位置参数求和
        stream.keyBy(t -> t.f0).sum(1).print("sum");
        // 按名称参数求和
        stream.keyBy(t -> t.f0).sum("f1").print("sum");

        stream.keyBy(r -> r.f0).max(1).print("max");
        stream.keyBy(r -> r.f0).max("f1").print("max");

        stream.keyBy(r -> r.f0).min(1).print("min");
        stream.keyBy(r -> r.f0).min("f1").print("min");

        stream.keyBy(r -> r.f0).maxBy(1).print("maxBy");
        stream.keyBy(r -> r.f0).maxBy("f1").print("maxBy");

        stream.keyBy(r -> r.f0).minBy(1).print("minBy");
        stream.keyBy(r -> r.f0).minBy("f1").print("minBy");

        env.execute();

    }
}
