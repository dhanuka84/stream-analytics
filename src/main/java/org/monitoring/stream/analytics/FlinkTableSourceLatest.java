package org.monitoring.stream.analytics;



import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.WindowedTable;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.table.api.java.Tumble;
import org.apache.flink.table.descriptors.Json;
import org.apache.flink.table.descriptors.Kafka;
import org.apache.flink.table.descriptors.Schema;

public class FlinkTableSourceLatest {

	public static void main(String[] args) throws Exception {
        // Read parameters from command line
		final ParameterTool params = ParameterTool.fromPropertiesFile("application.properties");

        if(params.getNumberOfParameters() < 4) {
            System.out.println("\nUsage: FlinkReadKafka " +
                               "--read-topic <topic> " +
                               "--write-topic <topic> " +
                               "--bootstrap.servers <kafka brokers> " +
                               "--group.id <groupid>");
            return;
        }

        // define a schema
      
        // setup streaming environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        env.getConfig().setRestartStrategy(RestartStrategies.fixedDelayRestart(4, 10000));
        env.enableCheckpointing(300000); // 300 seconds
        env.getConfig().setGlobalJobParameters(params);

        StreamTableEnvironment tableEnv = TableEnvironment.getTableEnvironment(env);

        tableEnv
        .connect(
          new Kafka()
            .version("0.10")
           .topic("testin")
           .property("zookeeper.connect", "localhost:2181")
           .property("bootstrap.servers", "localhost:9092")
           .property("group.id", "analytics")
           .startFromLatest())
        
        .withFormat(
          new Json()
           .jsonSchema("{\n" + 
            	"  \"type\": \"object\",\n" + 
            	"  \"properties\": {\n" + 
            	"    \"food\": {\n" + 
            	"      \"type\": \"string\"\n" + 
            	"    },\n" + 
            	"    \"price\": {\n" + 
            	"      \"type\": \"integer\"\n" + 
            	"    },\n" + 
            	"    \"processingTime\": {\n" + 
            	"      \"type\": \"integer\"\n" + 
            	"    }\n" + 
            	"  }\n" + 
            	"}")
            .failOnMissingField(false)
            .deriveSchema()
            )
        .withSchema(
          new Schema()
            .field("food", "VARCHAR")
            .field("price", "DECIMAL")
            .field("processingTime", "TIMESTAMP").proctime())
        .inAppendMode()
        
        .registerTableSource("foodTable");

        WindowedTable windowedTable = tableEnv.scan("foodTable").window(Tumble.over("50.minutes").on("processingTime").as("userActionWindow")); 
  
        tableEnv
        // declare the external system to connect to
        .connect(
        new Kafka()
          .version("0.10")
          .topic("testout")
          .startFromEarliest()
          .property("zookeeper.connect", "localhost:2181")
          .property("bootstrap.servers", "localhost:9092"))
        .withFormat(new Json()
          .failOnMissingField(false)
          .deriveSchema()
        )
        .withSchema(
                new Schema()
                  .field("food", "VARCHAR"))

        // specify the update-mode for streaming tables
        .inAppendMode()

        // register as source, sink, or both and under a name
        .registerTableSink("ruleTable");

        String sql ="SELECT food FROM foodTable";

        Table result = tableEnv.sqlQuery(sql);
        System.out.println("======================= "+result.toString());

        result.insertInto("ruleTable");

        env.execute("FlinkReadWriteKafkaJSON");
    }

}
