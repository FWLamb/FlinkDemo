package com.yang.flink.source;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author Bin
 * @date 2022/4/12 17:33
 * @Description 从文件读取数据
 */
public class ReadTextFileSource {
    public static void main(String[] args) throws Exception {
        // 1. 创建流式执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(12);
        /**
         * ⚫	路径可以是相对路径，也可以是绝对路径；
         * ⚫	相对路径是从系统属性 user.dir 获取路径: idea 下是 project 的根目录, standalone 模式下是集群节点根目录；
         * ⚫	也可以从hdfs 目录下读取, 使用路径hdfs://..., 由于 Flink 没有提供hadoop 相关依赖, 需要 pom 中添加相关依赖
         */
        // 2. 读取文件
        DataStreamSource<String> streamSource = env.readTextFile("input/clicks.csv");

        streamSource.print();


        env.execute();
    }
}
