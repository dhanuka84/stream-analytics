package org.monitoring.stream.analytics.functions;

import java.math.BigDecimal;
import java.util.List;

import org.monitoring.stream.analytics.model.Condition;
import org.monitoring.stream.analytics.model.Event;
import org.monitoring.stream.analytics.util.JSONUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.udojava.evalex.Expression;

public class ConditionValidator {
    private static final BigDecimal ZERO = new BigDecimal(0);

    public static boolean validate(final List<Condition> conditions, final Event event) {
	JsonNode node = JSONUtils.convertToJSON(event);
	StringBuilder decision = new StringBuilder();
	for(Condition con : conditions) {
	    boolean result = false;
		// 
		String operator = con.getOperator();
		String value = con.getValue();
		String dataType = con.getDataType();
		String attribute = con.getAttribute();
		String logical = con.getLogicalOperator();

		switch (operator) {
		case "=":
		    result = node.get(attribute).asText().equalsIgnoreCase(value);
		    break;
		case "regex":
		    result = node.get(attribute).asText().matches(".*" + value + ".*");
		    break;

		default:
		    Expression expression = new Expression(node.get(attribute).asText() + " " + operator + " " + value);
		    result = expression.eval().intValue() != ZERO.intValue();
		    break;

		}
		if(decision.length() > 0) {
		    decision.append(logical);
		}
		decision.append(" ").append(result);
		
	}
	

	return new Expression(decision.toString()).eval().intValue() != ZERO.intValue();
    }

}
