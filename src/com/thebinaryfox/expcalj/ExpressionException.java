package com.thebinaryfox.expcalj;

/**
 * An exception thrown when an error occurred which prevented the parsing or
 * evaluation of an Expression.
 * 
 * @author The Binary Fox
 */
public class ExpressionException extends RuntimeException {

	private static final long serialVersionUID = -5415708594042968578L;

	public ExpressionException(String str) {
		super(str);
	}

	public ExpressionException(String str, Throwable t) {
		super(str, t);
	}

}
