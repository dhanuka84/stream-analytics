package org.monitoring.stream.analytics.util;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;


public final class ApplicationConfig {
	public static volatile Properties CONFIG_PROPERTIES;

	public static void init() throws IOException {
		CONFIG_PROPERTIES = FileHandler.readEnvConfig();

	}
	
	public static final String QUERY_CONFIG = "query.config";
	public static final String JSON_SCHEMA_CONFIG = "json.schema.config";
	public static final String JOB_NAME = "jobname";
	public static final String FLINK_PARALLEL_TASK = "parallelism";
	public static final String FLINK_NUMBER_OF_OR_OPERATORS = "noOfOROperators";

	// In minutes
	public static final String HIBERNATE_SESSION_TIMEOUT = "sessionTimeOut";
	public static final String SESSION_CLOSE_WAIT_TIME = "sessionCloseWaitTime";
	public static final String HIBERNATE_TX_SESSION_TIMEOUT = "txSessionTimeOut";
	public static final String SESSION_TX_CLOSE_WAIT_TIME = "txSessionCloseWaitTime";
	public static final String CACHE_TIMEOUT = "cacheTimeout";

	// in seconds
	public static final String SCHEDULER_INITIAL_DELAY = "schedulerInitialDelay";
	public static final String SCHEDULER_DELAY = "schedulerDelay";
	public static final String HIBERNATE_LOCAL_CONFIG_KEY = "hibernate.localconfiguration";
	public static final String HIBERNATE_LOCAL_TRANSACTIONAL_CONFIG_KEY = "hibernate.transaction.config";
	public static final String HAZELCAST_CONFIG_KEY = "hazelcast.config";
	public static final String HAZELCAST_CLIENT_CONFIG_KEY = "hazelcast.client.config";
	public static final String HAZELCAST_INSTANCE_NAME = "hzname";
	public static final String KAFKA_CONSUMER_CONFIG_KEY = "kafka.consumer.config";
	public static final String KAFKA_PRODUCER_CONFIG_KEY = "kafka.producer.config";
	public static final String KAFKA_BULK_PROCESS_CONFIG_KEY = "kafka.bulk.config";
	public static final String ES_CLIENT_CONFIG_KEY = "es.client.config";
	public static final String HTTP_CONFIG_KEY = "http.client.config";
	public static final String SHIPPER_URL = "shipper.url";

	public static final String HTTP_LOCAL_CONFIG_KEY = "http.localconfiguration";
	public static final String HIBERNATE_FACTORY_NAME = "HibernateFactory";
	public static final String HIBERNATE_TRANSACTION_FACTORY_NAME = "HibernateTransactionFactory";
	public static final String HIBERNATE_SERVER_CONFIG_FILE = "hibernate.cfg.xml";
	public static final String ADMIN_PORT = "adminPort";
	public static final String WHITE_LIST_APPLICATIONS = "whitelistApps";
	public static final String ENABLE_KAFKA = "enableKafka";
	public static final String ENABLE_HTTP = "enableHTTP";
	public static final String ENABLE_ES_REST_CLIENT = "enableESRestClient";
	public static final String KAFKA_CONSUMER_TOPIC_MAPPING = "consumerTopicMapping";
	public static final String KAFKA_PRODUCER_TOPIC_MAPPING = "producerTopicMapping";
	public static final String DATA_CENTER_NAME = "dataCenterName";
	public static final String ENABLE_KAFKA_PRODUCER = "enableKafkaProducer";
	public static final String ENABLE_KAFKA_CONSUMER = "enableKafkaConsumer"; 
	public static final String KAFKA_CONSUMER_PROCESS_MAPPING = "kafkaProcessMapping";

	//Admin endPoints 
	public static final String FIND_APM_APPLICATIONS ="/admin.product/findApmMap";
	public static final String DC_LOCATION = "dc.location";
	public static final String DISTRIBUTED_MAP = "distributedMap";
	public static final String DEFAULT_DISTRIBUTED_MAP = "default";
	public static final String PARENT_ENTITY = "parentEntity";
	public static final String APM_DISTRIBUTED_MAP = "apm";
	public static final String DEPSYNCH_DISTRIBUTED_MAP = "masterDataSynch";

	// cyclone specific
	public static final String CYCLONE_CAN_SYNCH_DB = "canSynchToDB";
	public static final String ENABLE_MIRROR = "enableMirror";
	public static final String PASSIVE_ENABLE_MIRROR= "PassiveEnableMirror";
	public static final String CYCLONE_TOPIC_CONFIG = "cyclone.apm.topic";
	public static final String CYCLONE_CONFIGURATION_TOPIC = "cyclone.configuration.topic";
	public static final String CYCLONE_HOSTS = "cyclone.hosts";
	
	// Es specific
	public static final String CYCLONE_ES_INDEX = "cycloneEsIndex";
	public static final String CYCLONE_ES_TYPE = "cycloneEsType";
	
	//mirror maker
	public static final String TOPIC_MAPPINGS = "topicMappings";
	//cmdb hosts
	public static final String CMDB_HOSTS = "cmdb.hosts";
	//apm hosts
	public static final String APM_HOSTS = "apm.hosts";

	public static interface CollectorConfig {
		String SYNTHETIC_CONFIG_KEY = "synthetic.config";
		String INCIDENT_CONFIG_KEY = "incident.config";
		String TIME_INCIDENT_CONFIG_KEY = "time.incident.config";
		String CMDB_CONFIG_KEY = "cmdb.config";
		String HEALTH_CONFIG_KEY = "health.config";
		String EVENT_LISTENER_CONFIG_KEY = "eventlistener.config";
		String SYNTHETIC_FREQUENCY_CONFIG_KEY = "synthetic.frequency.config";
		String GOOGLE_ANALYTICS_CONFIG_KEY = "google.analytics.config";
		String MAINTENANCE_CONFIG_KEY = "maintenance.config";
		String ANALYTICS_QUERY_CONFIG_KEY = "analytics.query.config";
		String LOGGER_CONFIG_KEY = "logger.config";
		String APM_CONFIG_KEY = "apm.config";
		String MIRROR_CONFIG_KEY = "mirror.config";
		String RECOVERY_CONFIG_KEY = "recovery.config";
		String CACHE_CONFIG_KEY = "cache.config";
		String NEWRELIC_ACCOUNT_KEY_MAPPING = "newrelic.key.mapping";

	}

	public static Map<String, Object> convertToMap(final Properties properties, final Map<String, Object> map) {
		for (final String name : properties.stringPropertyNames())
			map.put(name, properties.getProperty(name));

		return map;
	}

}
