package com.yang.flink.streamconversion;

import org.apache.flink.streaming.api.datastream.ConnectedStreams;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.CoMapFunction;

/**
 * @author Bin
 * @date 2022/4/27 17:27
 * @Description
 */
public class CoMapExample {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        DataStreamSource<Integer> stream1 = env.fromElements(1, 2, 3);
        DataStreamSource<String> stream2 = env.fromElements("1", "2", "3");

        ConnectedStreams<Integer, String> connectedStreams = stream1.connect(stream2);

        SingleOutputStreamOperator<String> result = connectedStreams.map(new CoMapFunction<Integer, String, String>() {
            @Override
            public String map1(Integer value) throws Exception {
                return "Integer: " + value;
            }

            @Override
            public String map2(String value) throws Exception {
                return "String: " + value;
            }
        });

        result.print();

        env.execute();
    }
}
