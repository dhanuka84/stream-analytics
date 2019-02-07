package org.monitoring.stream.analytics.functions;

import java.sql.Timestamp;

import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.RichCoFlatMapFunction;
import org.apache.flink.util.Collector;
import org.monitoring.stream.analytics.model.Event;
import org.monitoring.stream.analytics.model.FilteredEvent;
import org.monitoring.stream.analytics.model.Rule;

public class FilterFunction extends RichCoFlatMapFunction<Rule, Event, FilteredEvent> {

    private static final long serialVersionUID = 1L;
    private ListState<Rule> rules;

    @Override
    public void open(Configuration conf) {
	ListStateDescriptor<Rule> descriptor = new ListStateDescriptor<>("configuration", Rule.class);
	rules = getRuntimeContext().getListState(descriptor);
    }

    @Override
    public void flatMap1(Rule rule, Collector<FilteredEvent> out) throws Exception {
	rules.add(rule);
    }

    public void flatMap2(Rule model, Collector<FilteredEvent> out) {
	/*
	 * if (threshold.value() == null || model.getData() > threshold.value()) {
	 * out.collect(model); }
	 */
    }

    @Override
    public void flatMap2(Event event, Collector<FilteredEvent> out) throws Exception {
	Iterable<Rule> iter = rules.get();
	for (Rule rule : iter) {
	    if (ConditionValidator.validate(rule.getConditions(), event)) {
		out.collect(new FilteredEvent(event.getDriverID(), rule.getAlertId(), event.getEventId(),
			new Timestamp(System.currentTimeMillis()).toString()));
	    }

	}

    }
}