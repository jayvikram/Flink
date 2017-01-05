package com.jay.poc;

import org.apache.flink.api.common.functions.FoldFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.wikiedits.WikipediaEditEvent;
import org.apache.flink.streaming.connectors.wikiedits.WikipediaEditsSource;

public class WikipediaAnalysis {
	public static void main(String[] args) throws Exception {
		StreamExecutionEnvironment see = StreamExecutionEnvironment.getExecutionEnvironment();
		DataStream<WikipediaEditEvent> edits = see.addSource(new WikipediaEditsSource());
		KeyedStream<WikipediaEditEvent, String> keyedEdits = edits.keyBy(new KeySelector<WikipediaEditEvent, String>() {
			public String getKey(WikipediaEditEvent event) throws Exception {
				return event.getUser();
			}
		});
		DataStream<Tuple2<String, Long>> result = keyedEdits.timeWindow(Time.seconds(5)).fold(new Tuple2<String, Long>("", 0L), new FoldFunction<WikipediaEditEvent, Tuple2<String, Long>>() {

			public Tuple2<String, Long> fold(Tuple2<String, Long> acc,
					WikipediaEditEvent event) throws Exception {
				acc.f0 = event.getUser();
				acc.f1 += event.getByteDiff();
				// TODO Auto-generated method stub
				return acc;
			}
		});
		result.print();
		see.execute();
	}
}
