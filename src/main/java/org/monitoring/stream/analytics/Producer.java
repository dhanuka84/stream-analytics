package org.monitoring.stream.analytics;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.monitoring.stream.analytics.model.Event;
import org.monitoring.stream.analytics.model.Rule;
import org.monitoring.stream.analytics.transform.EventParser;
import org.monitoring.stream.analytics.transform.RuleParser;
import org.monitoring.stream.analytics.util.FileHandler;
import org.monitoring.stream.analytics.util.JSONUtils;

/**

 */
public class Producer {

    public static void main(String[] argv) throws Exception {

	ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
	File file = new File(classLoader.getResource("small.json").getFile());
	File ruleFile = new File(classLoader.getResource("rule.json").getFile());
	Properties kProducer = FileHandler.loadResourceProperties("producer-test.properties");

	String content = FileUtils.readFileToString(file, "UTF-8");
	String rule = FileUtils.readFileToString(ruleFile, "UTF-8");

	// Configure the Producer
	// kProducer.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092");
	kProducer.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
		"org.apache.kafka.common.serialization.StringSerializer");
	kProducer.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
		"org.apache.kafka.common.serialization.StringSerializer");

	
	System.out.println( rule);
	Rule ruleObj = (Rule) JSONUtils.convertToObject(rule, Rule.class);
	RuleParser ruleParser = new RuleParser();
	System.out.println( ruleObj);
	

	
	ExecutorService executor = Executors.newFixedThreadPool(10);
	List<Future> results = new ArrayList<Future>();

	EventParser deserializer = new EventParser();

	Random random = new Random();
	String prefix = "A";

	int rides = 100;
	for (int x = 0; x < rides; ++x) {
	    final int z = x;
	    final boolean bool = random.nextBoolean();
	    Future<Integer> result = executor.submit(() -> {
		String driverId = (bool + ":" + z);
		
		org.apache.kafka.clients.producer.Producer ruleProducer = new KafkaProducer(kProducer);
		for (int i = 0; i < 10; ++i) {
		    
		    String ruleStr = new String(ruleParser.serialize(ruleObj));
		    ruleObj.setDriverID(driverId);
		    //System.out.println("=================== Rule Obj " + ruleStr);
		    ProducerRecord<String, String> rec = new ProducerRecord<String, String>("rule", ruleStr);
		    ruleProducer.send(rec);
		}
		ruleProducer.close();
		
		String threadName = Thread.currentThread().getName();
		long tId = Thread.currentThread().getId();
		System.out.println("Hello " + threadName);
		org.apache.kafka.clients.producer.Producer producer = new KafkaProducer(kProducer);
		
		int count = 10000;
		System.out.println("count " + count + "===================## start time " + new Date());
		
		
		
		UUID uuid = UUID.randomUUID();
		for (int i = 0; i < count; ++i) {
		    String id = (bool + ":" + z + ":" + i);
		    Event event = deserializer.deserialize(content.getBytes());
		    event.setEventId(id);
		    event.setDriverID(driverId);
		    ProducerRecord<String, String> rec = new ProducerRecord<String, String>("testin",
			    new String(deserializer.serialize(event)));
		    producer.send(rec);
		}

		producer.close();
		System.out.println("=================== end time " + new Date());
		return 123;

	    });
	    results.add(result);

	}
	 

	/*
	 * for(Future result : results) { result.get(); }
	 */

    }

}