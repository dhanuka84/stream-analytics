package org.monitoring.stream.analytics.functions;

import org.monitoring.stream.analytics.model.FilteredEvent;
import org.monitoring.stream.analytics.util.JSONUtils;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;

public class TransformFunction implements FlatMapFunction<FilteredEvent,String>{

    private static final long serialVersionUID = 1L;

    @Override
    public void flatMap(FilteredEvent value, Collector<String> out) throws Exception {
	out.collect(JSONUtils.convertToString(value));
	
    }

}
