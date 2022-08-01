package com.yang.flink.transformation;

import com.yang.flink.pojo.Event;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author Bin
 * @date 2022/4/13 9:32
 * @Description
 */
public class FilterTransTest {
    public static void main(String[] args) throws Exception {
        /**
         * 过滤用户Bob的行为
         */
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStreamSource<Event> dataStreamSource = env.fromElements(new Event("Bob", "./home", 1000L),
                new Event("Tom", "./usr", 2000L));

        // 1.
        dataStreamSource.filter(new FilterFunction<Event>() {
            @Override
            public boolean filter(Event event) throws Exception {
                return event.user.equals("Bob");
            }
        }).print();

        // 2.
        dataStreamSource.filter(new UserFilter()).print();

        env.execute();
    }

    private static class UserFilter implements FilterFunction<Event> {
        @Override
        public boolean filter(Event event) throws Exception {
            return event.user.equals("Bob");
        }
    }
}
