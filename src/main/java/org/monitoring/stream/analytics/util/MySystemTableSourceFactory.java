package org.monitoring.stream.analytics.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.flink.table.factories.StreamTableSourceFactory;
import org.apache.flink.table.sources.StreamTableSource;
import org.apache.flink.types.Row;

class MySystemTableSourceFactory implements StreamTableSourceFactory<Row> {

  @Override
  public Map<String, String> requiredContext() {
    Map<String, String> context = new HashMap<>();
    context.put("update-mode", "append");
    context.put("connector.type", "my-system");
    return context;
  }

  @Override
  public List<String> supportedProperties() {
    List<String> list = new ArrayList<>();
    list.add("connector.debug");
    return list;
  }

  @Override
  public StreamTableSource<Row> createStreamTableSource(Map<String, String> properties) {
    boolean isDebug = Boolean.valueOf(properties.get("connector.debug"));

    // additional validation of the passed properties can also happen here

    return new MySystemAppendTableSource();
  }
}
