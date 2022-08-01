package com.yang.flink.transformation.partition;

import com.yang.flink.pojo.Event;
import com.yang.flink.source.ClickSource;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author Bin
 * @date 2022/4/13 10:38
 * @Description
 */
public class ShufflePartitionTest {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env =
                StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        // 读取数据源，并行度为 1
        DataStreamSource<Event> stream = env.addSource(new ClickSource());

        // 经洗牌后打印输出， 并行度为4
        stream.shuffle().print("shuffle").setParallelism(4);

        env.execute();

    }
}
