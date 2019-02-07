package org.monitoring.stream.analytics.util;

import com.udojava.evalex.Expression;

public class Test {
    
    public static void main(String ...strings) {
	
	String regex = "193400835";
	String value = "60193400835_011";
	System.out.println(value.matches(".*193400835.*"));
	
	 Expression expression = new Expression( "true && true");
	    boolean result = expression.eval().intValue() != 0;
	    System.out.println(result);
	
    }

}
