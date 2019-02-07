package org.monitoring.stream.analytics;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.TaskManagerOptions;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.Types;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.table.descriptors.Json;
import org.apache.flink.table.descriptors.Kafka;
import org.apache.flink.table.descriptors.Rowtime;
import org.apache.flink.table.descriptors.Schema;
import org.apache.flink.table.functions.ScalarFunction;
import org.apache.flink.table.shaded.org.apache.commons.lang3.StringUtils;
import org.monitoring.stream.analytics.util.ApplicationConfig;
import org.monitoring.stream.analytics.util.FileHandler;
import org.monitoring.stream.analytics.util.HazelcastUtils;
import org.monitoring.stream.analytics.util.MySystemConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MultiMap;

public class FlinkDynamicEmbedded {
    private final static Logger LOGGER = LoggerFactory.getLogger(FlinkDynamicEmbedded.class);

    public static void main(String[] args) throws Exception {
	String homeFolder = "/home/dhanuka/projects/flink-research/stream-analytics/remote/";
	String query = FileHandler.readInputStream(FileHandler.getFileContentAsBytes(homeFolder+"query.sql"));
	if (query == null) {
	    LOGGER.error("*****************  Can't read resources ************************");
	} else {
	    LOGGER.info("======================== " + query + " =============================");
	}
	
	String content = FileHandler.readInputStream(FileHandler.getFileContentAsBytes(homeFolder+"small.json"));
	Properties props = FileHandler.loadPropertiesFromFile(homeFolder+"application.properties");
	Properties kConsumer = FileHandler.loadPropertiesFromFile(homeFolder+"consumer.properties");
	Properties kProducer = FileHandler.loadPropertiesFromFile(homeFolder+"producer.properties");
	String hzConfig = FileHandler.readInputStream(FileHandler.getFileContentAsBytes(homeFolder+"hazelcast-client.xml"));
	String schemaContent = FileHandler.readInputStream(FileHandler.getFileContentAsBytes(homeFolder+"schema.json"));

	props.setProperty("auto.offset.reset", "latest");
	props.setProperty("flink.starting-position", "latest");
	Map<String, String> tempMap = new HashMap<>();
	for (final String name : props.stringPropertyNames())
	    tempMap.put(name, props.getProperty(name));
	final ParameterTool params = ParameterTool.fromMap(tempMap);
	String jobName = props.getProperty(ApplicationConfig.JOB_NAME);

	LOGGER.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Desktop Responsibility Start %%%%%%%%%%%%%%%%%%%%%%");
	LOGGER.info("============================== schema " + schemaContent);
	LOGGER.info("$$$$$$$$$$$$$$$$$$$$$$$ Hz instance name " + props.toString());
	HazelcastInstance hzInst = HazelcastUtils.getClient(hzConfig, "");

	

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
	if(StringUtils.isNoneBlank(noOfOROperatorsValue)) {
	    noOfOROperators = Integer.parseInt(noOfOROperatorsValue);
	}
	
	List<List<String>> subQueries = chunk(new ArrayList(rules), noOfOROperators);

	// define a schema

	// setup streaming environment
	Configuration config = new Configuration();
	config.setInteger(TaskManagerOptions.NUM_TASK_SLOTS, 25);
	StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(config);
	// env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
	env.getConfig().setRestartStrategy(RestartStrategies.fixedDelayRestart(4, 10000));
	env.enableCheckpointing(300000); // 300 seconds
	env.getConfig().setGlobalJobParameters(params);
	// env.getConfig().enableObjectReuse();
	env.getConfig().setUseSnapshotCompression(true);
	env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
	env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.AT_LEAST_ONCE);
	env.setParallelism(Integer.parseInt(paral));
	/* env.setStateBackend(new RocksDBStateBackend(env.getStateBackend(), true)); */

	StreamTableEnvironment tableEnv = TableEnvironment.getTableEnvironment(env);
	tableEnv.registerFunction("mytime", new MyTime(10));

	tableEnv.connect(new MySystemConnector())

		.withFormat(new Json().jsonSchema(schemaContent).failOnMissingField(false).deriveSchema())
		.withSchema(new Schema().field("HireID", "DECIMAL").field("PassengerName", "VARCHAR")
			.field("CorrelationID", "VARCHAR").field("DriverID", "DECIMAL").field("ts", Types.SQL_TIMESTAMP())
			.rowtime(new Rowtime().timestampsFromSource().watermarksPeriodicBounded(1000)))
		.inAppendMode()

		.registerTableSource(sourceTable);

	
	List<String> data = new ArrayList<>();
	data.add(content);
	env.fromCollection(data);
	// tableEnv.sqlQuery(query)
	StringBuilder unionQuery = new StringBuilder();
	for (List<String> subQuery : subQueries) {
	    StringBuilder orQuery = new StringBuilder();
	    for (String sql : subQuery) {
		if (orQuery.length() > 0) {

		    orQuery.append(" OR ");
		}
		orQuery.append(getOnlyConditions(sql));

	    }
	    String singleQuery = " SELECT DriverID,'Rule1' as RuleName,mytime(ts) as ts1 , mytime(CURRENT_TIMESTAMP) as ts2 FROM dataTable WHERE "+ orQuery.toString();
	    if (unionQuery.length() > 0) {
		unionQuery.append(" UNION ALL ");
	    }
	    unionQuery.append(singleQuery);
	}
	
	LOGGER.info("********************************* " + unionQuery.toString());
	Table result = tableEnv.sqlQuery(unionQuery.toString());
	// DataStream<Tuple2<Boolean, Row>> messageStream =
	// tableEnv.toRetractStream(result, Row.class);

	/*tableEnv
		// declare the external system to connect to
		.connect(new Kafka().version("0.10").topic("testout").startFromEarliest().properties(kProducer))

		.withFormat(new Json().failOnMissingField(false).deriveSchema())
		.withSchema(new Schema().field("DriverID", Types.DECIMAL()).field("RuleName", Types.STRING())
			.field("ts1", Types.STRING()).field("ts2", Types.STRING()))

		// specify the update-mode for streaming tables
		.inAppendMode()

		// register as source, sink, or both and under a name
		.registerTableSink("ruleTable");

	// tableEnv.sqlUpdate( "INSERT INTO ruleTable " + result);

	result.insertInto("ruleTable");*/
	// messageStream.print();

	env.execute(props.getProperty(ApplicationConfig.JOB_NAME));
	System.out.println("======================= Successfully Deployed ========================");
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
	int count = (rest == 0 ? noOfChunks: noOfChunks +1);

	System.out.println("rest  " + rest + " noOfChunks " + noOfChunks);
	List<List<String>> chunks = new ArrayList<>();
	for (int index = 0; index < count; ++index) {
	    if (index == 0 && rest > 0) {
		end = rest;
	    }else {
		end = start + chunkSize;
	    }

	    List<String> sublist = arrayList.subList(start, end);
	    start = end;
	    chunks.add(sublist);
	    //System.out.println(sublist);
	}
	return chunks;

    }
    
    public static String getOnlyConditions(final String sql) {
	int index = sql.indexOf("WHERE");
	return sql.substring(index).replace("WHERE", "(") + ")";
    }
}