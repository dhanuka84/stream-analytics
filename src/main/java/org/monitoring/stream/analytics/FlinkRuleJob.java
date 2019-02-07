package org.monitoring.stream.analytics;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010;
import org.apache.flink.table.shaded.org.apache.commons.lang3.StringUtils;
import org.monitoring.stream.analytics.functions.FilterFunction;
import org.monitoring.stream.analytics.functions.TransformFunction;
import org.monitoring.stream.analytics.model.Event;
import org.monitoring.stream.analytics.model.FilteredEvent;
import org.monitoring.stream.analytics.model.Rule;
import org.monitoring.stream.analytics.transform.EventParser;
import org.monitoring.stream.analytics.transform.RuleParser;
import org.monitoring.stream.analytics.transform.StringSerializer;
import org.monitoring.stream.analytics.util.ApplicationConfig;
import org.monitoring.stream.analytics.util.FileHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlinkRuleJob {
    private final static Logger LOGGER = LoggerFactory.getLogger(FlinkRuleJob.class);

    public static void main(String[] args) throws Exception {

	String query = FileHandler.readInputStream(FileHandler.getResourceAsStream("query.sql"));
	if (query == null) {
	    LOGGER.error("*****************  Can't read resources ************************");
	} else {
	    LOGGER.info("======================== " + query + " =============================");
	}
	Properties props = FileHandler.loadResourceProperties("application.properties");
	Properties kConsumer = FileHandler.loadResourceProperties("consumer.properties");
	Properties kProducer = FileHandler.loadResourceProperties("producer.properties");
	String hzConfig = FileHandler.readInputStream(FileHandler.getResourceAsStream("hazelcast-client.xml"));
	String schemaContent = FileHandler.readInputStream(FileHandler.getResourceAsStream("schema.json"));

	props.setProperty("auto.offset.reset", "latest");
	props.setProperty("flink.starting-position", "latest");
	Map<String, String> tempMap = new HashMap<>();
	for (final String name : props.stringPropertyNames())
	    tempMap.put(name, props.getProperty(name));
	final ParameterTool params = ParameterTool.fromMap(tempMap);
	String jobName = props.getProperty(ApplicationConfig.JOB_NAME);

	final String sourceTable = "dataTable";

	String paral = props.getProperty(ApplicationConfig.FLINK_PARALLEL_TASK);
	String noOfOROperatorsValue = props.getProperty(ApplicationConfig.FLINK_NUMBER_OF_OR_OPERATORS);
	int noOfOROperators = 50;
	if (StringUtils.isNoneBlank(noOfOROperatorsValue)) {
	    noOfOROperators = Integer.parseInt(noOfOROperatorsValue);
	}

	// define a schema

	// setup streaming environment
	StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
	// env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
	env.getConfig().setRestartStrategy(RestartStrategies.fixedDelayRestart(4, 10000));
	env.enableCheckpointing(300000); // 300 seconds
	env.getConfig().setGlobalJobParameters(params);
	// env.getConfig().enableObjectReuse();
	env.getConfig().setUseSnapshotCompression(true);
	// env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
	env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.AT_LEAST_ONCE);
	env.setParallelism(Integer.parseInt(paral));

	/* env.setStateBackend(new RocksDBStateBackend(env.getStateBackend(), true)); */

	FlinkKafkaConsumer010<Event> eventSource = new FlinkKafkaConsumer010<>("testin", new EventParser(), kConsumer);
	FlinkKafkaConsumer010<Rule> ruleSource = new FlinkKafkaConsumer010<>("rule", new RuleParser(), kConsumer);
	/*
	 * kafka.assignTimestampsAndWatermarks(new TimestampAndWatermarkGenerator());
	 */
	final SourceFunction<Event> source = eventSource;
	DataStream<Event> events = env.addSource(source, "kafka").name("Event Source").setParallelism(Integer.parseInt(paral));
	events.rebalance();

	DataStream<Rule> rules = env.addSource(ruleSource, "kafka").name("Rule Source").setParallelism(Integer.parseInt(paral));
	events.rebalance();

	KeyedStream<Event, String> filteredEvents = events.keyBy(Event::getDriverID);

	//KeyedStream<Rule, Integer> filteredRules = rules.keyBy(Rule::getDriverID);

	DataStream<FilteredEvent> joinedStream = rules.broadcast().keyBy(Rule::getDriverID).connect(filteredEvents)
		.flatMap(new FilterFunction());
	DataStream<String> transformedStream = joinedStream.flatMap(new TransformFunction());

	// result.print();
	// result.rebalance();
	FlinkKafkaProducer010<String> producer = new FlinkKafkaProducer010<>("testout", new StringSerializer(),
		kProducer);
	transformedStream.addSink(producer).name("saveResults").setParallelism(Integer.parseInt(paral));

	env.execute(props.getProperty(ApplicationConfig.JOB_NAME));
	System.out.println("======================= Successfully Deployed ========================");
    }

}
