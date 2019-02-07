package org.monitoring.stream.analytics;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.collector.selector.OutputSelector;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.SplitStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010;
import org.apache.flink.table.descriptors.Kafka;
import org.apache.flink.table.functions.ScalarFunction;
import org.apache.flink.table.shaded.org.apache.commons.lang3.StringUtils;
import org.apache.flink.util.Collector;
import org.monitoring.stream.analytics.model.Event;
import org.monitoring.stream.analytics.transform.EventParser;
import org.monitoring.stream.analytics.transform.StringSerializer;
import org.monitoring.stream.analytics.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MultiMap;

import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternFlatSelectFunction;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.shaded.curator.org.apache.curator.shaded.com.google.common.base.Optional;
import org.apache.flink.streaming.api.windowing.time.Time;

import org.apache.flink.streaming.connectors.kafka.partitioner.FlinkKafkaPartitioner;

public class FlinkCEP {
    private final static Logger LOGGER = LoggerFactory.getLogger(FlinkCEP.class);

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

	LOGGER.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Desktop Responsibility Start %%%%%%%%%%%%%%%%%%%%%%");

	LOGGER.info("$$$$$$$$$$$$$$$$$$$$$$$ Hz instance name " + props.toString());
	HazelcastInstance hzInst = HazelcastUtils.getClient(hzConfig, "");

	LOGGER.info("============================== schema " + schemaContent);

	MultiMap<String, String> distributedMap = hzInst.getMultiMap("masterDataSynch");
	distributedMap.put(jobName, query);

	LOGGER.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Desktop Responsibility End %%%%%%%%%%%%%%%%%%%%%%%%%");

	Collection<String> queries = distributedMap.get(jobName);
	Set<String> rules = new HashSet<>(queries);
	LOGGER.info("============================== query" + query);
	rules.add(query);
	hzInst.getLifecycleService().shutdown();
	final String sourceTable = "dataTable";

	String paral = props.getProperty(ApplicationConfig.FLINK_PARALLEL_TASK);
	String noOfOROperatorsValue = props.getProperty(ApplicationConfig.FLINK_NUMBER_OF_OR_OPERATORS);
	int noOfOROperators = 50;
	if (StringUtils.isNoneBlank(noOfOROperatorsValue)) {
	    noOfOROperators = Integer.parseInt(noOfOROperatorsValue);
	}

	List<List<String>> subQueries = chunk(new ArrayList(rules), noOfOROperators);

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

	FlinkKafkaConsumer010<Event> kafka = new FlinkKafkaConsumer010<>("testin", new EventParser(), kConsumer);
	/*
	 * kafka.assignTimestampsAndWatermarks(new TimestampAndWatermarkGenerator());
	 */
	final SourceFunction<Event> source = kafka;
	DataStream<Event> events = env.addSource(source, "kafka").setParallelism(Integer.parseInt(paral));
	events.rebalance();

	
	  KeyedStream<Event, String> filteredEvents = events 
	 .keyBy(Event::getEventId);
	 

	// filteredEvents.print();
	  final List<DataStream<String>> results = new ArrayList<>();
	  for(int x=0 ; x < 1000 ; ++x) {
	   // create pattern
		Pattern<Event, Event> pattern = Pattern.<Event>begin("start").where(
			
			
			new SimpleCondition<Event>() {

			    private static final long serialVersionUID = 1L;

			    @Override
			    public boolean filter(Event event) throws Exception {
				boolean eval = event.getHireID().equals(4508724)
					&& event.getProviderInfo().equals("Dhanuka")
					&& event.getCorrelationID().matches(".*193400835.*");
				// System.out.println("================================== eval 1"+eval);
				return eval;
			    }
			})
			/*
			.or(new SimpleCondition<Event>() {

		    private static final long serialVersionUID = 1L;

		    @Override
		    public boolean filter(Event event) throws Exception {
			boolean eval = event.getHireID().equals(4508724) && event.getProviderInfo().equals("Dhanuka")
				&& event.getCorrelationID().matches(".*193400835.*");
			// System.out.println("================================== eval 2"+eval);
			return eval;
		    }
		})*/;
		
		PatternStream<Event> patternStream = CEP.pattern(filteredEvents, pattern);

		DataStream<String> result = patternStream.select(new PatternSelectFunction<Event, String>() {
		    private static final long serialVersionUID = 1L;

		    /*
		     * @Override public Alert select(Map<String, List<Event>> pattern) throws
		     * Exception { return parseMatch(pattern); }
		     */

		    @Override
		    public String select(Map<String, List<Event>> pattern) throws Exception {
			return parseMatch(pattern);
		    }

		    /*
		     * private Alert parseMatch(Map<String, List<Event>> pattern) { List<Event>
		     * events = pattern.get("start"); Alert alert = null; try { alert = new
		     * Alert(JSONUtils.writeListToJsonArray(events)); } catch (IOException e) {
		     * LOGGER.error(e.getMessage()); alert = new Alert("[]"); } return alert; }
		     */

		    private String parseMatch(Map<String, List<Event>> pattern) {
			List<Event> events = pattern.get("start");
			String alert = null;
			try {
			    alert = JSONUtils.convertToString(events);
			} catch (Exception e) {
			    LOGGER.error(e.getMessage());
			    alert = "[]";
			}
			return alert;
		    }

		    /*
		     * @Override public void flatSelect(Map<String, List<Event>> pattern,
		     * Collector<String> out) throws Exception { out.collect(parseMatch(pattern));
		     * 
		     * }
		     */

		});
		
		results.add(result);

	  }
		
	
	
	  
	// result.print();
	//result.rebalance();
	FlinkKafkaProducer010<String> producer = new FlinkKafkaProducer010<>("testout", new StringSerializer(),
		kProducer);
	
	for(DataStream<String> result : results) {
	    result.rebalance();
	    result.addSink(producer).name("saveResults").setParallelism(Integer.parseInt(paral));
	}
	

	env.execute(props.getProperty(ApplicationConfig.JOB_NAME));
	System.out.println("======================= Successfully Deployed ========================");
    }

    public static class FlatSelectNothing implements PatternFlatSelectFunction<Event, String> {

	private static final long serialVersionUID = 1L;

	@Override
	public void flatSelect(Map<String, List<Event>> pattern, Collector<String> collector) {
	    collector.collect((String) "LATE!");
	}
    }

    public static class MyTime extends ScalarFunction {

	private static final long serialVersionUID = 1L;

	public MyTime(long timeMills) {
	    super();
	}

	public String eval(long timeMills) {
	    return new Timestamp(timeMills).toString();
	}

    }

    public static class Now extends ScalarFunction {

	private static final long serialVersionUID = 1L;

	public Now() {
	    super();
	}

	public String eval() {
	    return new Timestamp(System.currentTimeMillis()).toString();
	}

    }

    public static List<List<String>> chunk(final List<String> arrayList, final int chunkSize) {

	int rest = arrayList.size() % chunkSize;
	int noOfChunks = arrayList.size() / chunkSize;
	int start = 0;
	int end = 0;
	int count = (rest == 0 ? noOfChunks : noOfChunks + 1);

	System.out.println("rest  " + rest + " noOfChunks " + noOfChunks);
	List<List<String>> chunks = new ArrayList<>();
	for (int index = 0; index < count; ++index) {
	    if (index == 0 && rest > 0) {
		end = rest;
	    } else {
		end = start + chunkSize;
	    }

	    List<String> sublist = arrayList.subList(start, end);
	    start = end;
	    chunks.add(sublist);
	    // System.out.println(sublist);
	}
	return chunks;

    }

    public static String getOnlyConditions(final String sql) {
	int index = sql.indexOf("WHERE");
	return sql.substring(index).replace("WHERE", "(") + ")";
    }
}