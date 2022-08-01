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
public class RebalancedPartitionTest {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env =
                StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        // 读取数据源，并行度为 1
        DataStreamSource<Event> stream = env.addSource(new ClickSource());

        // 经轮询重分区后打印输出 ， 并行度为4
        stream.rebalance().print("Rebalanced").setParallelism(4);

        env.execute();

    }
}
