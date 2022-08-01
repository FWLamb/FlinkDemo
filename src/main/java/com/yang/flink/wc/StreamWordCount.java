package com.yang.flink.wc;


import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * @author Bin
 * @date 2022/4/12 11:46
 * @Description 流处理
 */
public class StreamWordCount {
    public static void main(String[] args) throws Exception {
        // 1. 创建流式执行环境
        StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();

        // 从参数中提取主机名和端口号
        //ParameterTool parameterTool = ParameterTool.fromArgs(args);
        //String hostname = parameterTool.get("host", "hadoop102");
        //Integer port = parameterTool.getInt("port", 8888);

        // 2. 读取文件流
        DataStreamSource<String> lineDataStream = environment.socketTextStream("hadoop102", 7777);

        // 3. 转换计算
        SingleOutputStreamOperator<Tuple2<String, Long>> wordAndOneTuple = lineDataStream.flatMap(
                (String line, Collector<Tuple2<String, Long>> out) -> {
                    String[] words = line.split(" ");
                    for (String word : words) {
                        out.collect(Tuple2.of(word, 1L));
                    }
                }).returns(Types.TUPLE(Types.STRING, Types.LONG)); //当 Lambda 表达式使用 Java 泛型的时候, 由于泛型擦除的存在, 需要显示的声明类型信息

        // 4. 分组
        KeyedStream<Tuple2<String, Long>, String> wordAndOneKeyedStream = wordAndOneTuple.keyBy(data -> data.f0);

        // 5. 求和
        SingleOutputStreamOperator<Tuple2<String, Long>> sum = wordAndOneKeyedStream.sum(1);

        // 6. 输出
        sum.print();

        // 7. 启动执行
        environment.execute();

    }


}
