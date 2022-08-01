package com.yang.flink.source;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author Bin
 * @date 2022/4/12 17:38
 * @Description 从Socket读取数据
 */
public class SocketTextStreamSource {
    public static void main(String[] args) throws Exception {
        // 1. 创建流式执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(12);

        // 2. 读取数据
        DataStreamSource<String> streamSource = env.socketTextStream("hadoop102", 7777);

        streamSource.print();

        env.execute();
    }
}
