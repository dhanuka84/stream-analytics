package org.monitoring.stream.analytics.util;

public class ApplicationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3816214469310369843L;

	public ApplicationException(String message) {
		super(message);
	}

	public ApplicationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ApplicationException(Throwable ex) {
		super(ex);
	}

	public static String getStackTrace(Throwable th) {
		StringBuilder error = new StringBuilder();
		Throwable cause = (th.getCause() == null ? th
				: (th.getCause().getCause() == null ? th.getCause() : th.getCause().getCause()));
		error.append(cause.toString()).append("\n");
		StackTraceElement[] stacks = th.getStackTrace();
		for (StackTraceElement stack : stacks) {
			error.append(stack.toString()).append("\n");
		}
		return error.toString();
	}


	public static String getFirstLevelStackTrace(Throwable th) {
		StringBuilder error = new StringBuilder();
		Throwable cause = (th.getCause() == null ? th : th.getCause());
		error.append(cause.toString()).append("\n");
		StackTraceElement[] stacks = th.getStackTrace();
		for (StackTraceElement stack : stacks) {
			error.append(stack.toString()).append("\n");
		}
		return error.toString();
	}
}
