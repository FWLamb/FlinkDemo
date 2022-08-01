package com.yang.flink.source;

import com.yang.flink.pojo.Event;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.ParallelSourceFunction;

import java.util.Random;

/**
 * @author Bin
 * @date 2022/4/12 17:59
 * @Description 测试自定义数据源
 */
public class TestClickSource {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        /**
         * 这里要注意的是 SourceFunction 接口定义的数据源，并行度只能设置为 1，
         * 如果数据源设置为大于 1 的并行度，则会抛出异常:
         *  The parallelism of non parallel operator must be 1.
         *
         *  如果我们想要自定义并行的数据源的话，需要使用 ParallelSourceFunction
         */
        //env.setParallelism(1);

        //有了自定义的 source function，调用 addSource 方法
        DataStreamSource<Integer> stream = env.addSource(new CustomSource()).setParallelism(2);
        stream.print("SourceCustom");
        env.execute();
    }

    public static class CustomSource implements ParallelSourceFunction<Integer> {
        private boolean running = true;
        private Random random = new Random();

        @Override
        public void run(SourceContext<Integer> ctx) throws Exception {
            while (running) {
                ctx.collect(random.nextInt());
            }
        }

        @Override
        public void cancel() {
            running = false;
        }
    }
}
