package com.yang.flink.source;

import com.yang.flink.pojo.Event;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.ArrayList;

/**
 * @author Bin
 * @date 2022/4/12 17:26
 * @Description 从集合中读取数据
 */
public class FromCollectionSource {
    public static void main(String[] args) throws Exception {
        // 1. 创建流式执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(12);

        ArrayList<Event> clicks = new ArrayList<>();
        clicks.add(new Event("Marry", "./home", 1000L));
        clicks.add(new Event("Bob", "./cart", 1000L));

        // 2. 从集合中读取数据
        DataStreamSource<Event> streamSource = env.fromCollection(clicks);
        // 也可以不构建集合，直接将元素列举出来
        DataStreamSource<Event> streamSource1 = env.fromElements(new Event("Marry1", "./home", 1000L),
                new Event("Bob1", "./cart", 1000L));

        // 3. 打印
        streamSource.print();
        streamSource1.print();

        // 4. 执行
        env.execute();
    }
}
